/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.serial.{ComPort, DataCollector, DownloadState, ProcessWithProgress, Rc210}
import org.apache.pekko.stream.Materializer
import play.api.mvc.*

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class DownloadController @Inject()(config: Config, dataCollector: DataCollector, rc210: Rc210)
                                  (implicit ec: ExecutionContext, mat: Materializer, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  private val expectedLines: Int = config.getInt("vizRc210.expectedRcLines")

  def index: Action[AnyContent] = Action {
    Ok(views.html.download(rc210.comPort))
  }

  def startDownload: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val values: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded

      val comment = (for {
        map: Map[String, Seq[String]] <- values
        comments <- map.get("comment")
        comment <- comments.headOption
      } yield {
        comment
      })

      val requestTable = Table(Header("Download from RC210", "Field","Value"), Seq(
        Row.ofAny("ComPort", rc210.comPort),
        Row.ofAny("Comment", comment),
        Row.ofAny("Expecting", expectedLines),
      ))
      dataCollector.newDownload(requestTable)
      val webSocketURL: String = controllers.routes.DownloadController.ws().webSocketURL()

      Ok(views.html.progress(webSocketURL, requestTable, routes.DownloadController.results.url))
  }

  def ws(): WebSocket = {
    val p: ProcessWithProgress = ProcessWithProgress(dataCollector.progressMod, None)(progressApi =>
      dataCollector.startDownload(progressApi)
    )
    p.webSocket
  }

  def results: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.downloadResult(dataCollector.downloadState))
  }
}

