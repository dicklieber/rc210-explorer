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

import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.DataStore
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.serial.{ComPort, ERamCollector, RC210Data, RC210Download}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IOController @Inject()(implicit val controllerComponents: ControllerComponents,
                             dataStore: DataStore,
                             datFile: DatFile,
                             executionContext: ExecutionContext
                            ) extends BaseController with LazyLogging {
  implicit val timeout: Timeout = 5.seconds


  def progress(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val sJson: String = eramCollector.map { eRamCollector =>
        val progress1 = eRamCollector.progress
        val jsObject = Json.toJson(progress1)
        Json.prettyPrint(jsObject)
      }.getOrElse("no eramCollector")
      Ok(sJson)
  }

  def downloadResult: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      eramCollector match {
        case Some(eramCollector: ERamCollector) =>
          val table: Table = eramCollector.resultStatus.toTable
          Ok(views.html.RC210DownloadLandings(table))
        case None =>
          Ok("Download not performed!")
      }
  }

  private var eramCollector: Option[ERamCollector] = None

  def download(serialPortDescriptor: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val ec = new ERamCollector(serialPortDescriptor)
      eramCollector = Option(ec)

      val eventualRC210Data: Future[RC210Data] = ec.start()
      eventualRC210Data.foreach { r: RC210Data =>
        datFile((r))

      }
      Ok(views.html.RC210DownloadProgress())
  }

  def listSerialPorts(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val ports: List[ComPort] = RC210Download.listPorts
      val rows = ports.sortBy(_.friendlyName)
        .filterNot(_.descriptor.contains("/dev/tty")) // these are just clutter.
        .map { port => {
          val value: String = routes.IOController.download(port.descriptor).url
          Row(Cell(port.descriptor)
            .withUrl(value),
            port.friendlyName)
        }
        }
      val table = Table(Header("Serial Ports", "Descriptor", "Friendly Name"), rows)

      Ok(views.html.RC210DownloadLandings(table))
  }
}