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
import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.serial.{ComPort, DataCollector, ProcessWithProgress, Rc210}
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.stream.Materializer
import play.api.mvc.*

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class DownloadController @Inject()(config: Config, dataCollector: DataCollector, rc210: Rc210)
                                  (implicit scheduler: Scheduler, ec: ExecutionContext, mat: Materializer, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  private val expectedLines: Int = config.getInt("vizRc210.expectedRcLines")
  private var maybeComment: Option[String] = None


  def index: Action[AnyContent] = Action {
    Ok(views.html.download(rc210.comPort))
  }


  def startDownload: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val values: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded

      maybeComment = (for {
        map: Map[String, Seq[String]] <- values
        comments <- map.get("comment")
        comment <- comments.headOption
      } yield {
        comment
      })

      val table: Table = Table(Header("Download from RC210"), Seq(
        Row.ofAny("ComPort", rc210.comPort),
        Row.ofAny("Comment", maybeComment),
        Row.ofAny("Expecting", expectedLines),
      ))

      val webSocketURL: String = controllers.routes.DownloadController.ws(expectedLines).webSocketURL()
      Ok(views.html.progress(webSocketURL, Option(table)))
  }

  def ws(expected: Int): WebSocket = {
    ProcessWithProgress(expected, dataCollector.progreMod, None)(progressApi =>
      dataCollector(progressApi, maybeComment)
    )
  }

}

case class DownloadMetadata(comment: String, comPort: ComPort, start: Instant = Instant.now())