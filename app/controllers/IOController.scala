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
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.DataStore
import net.wa9nnn.rc210.serial.{ComPort, RC210Actor, RC210Download}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}
import akka.actor.typed.ActorRef
import net.wa9nnn.rc210.serial.RC210Actor.StartDownload
@Singleton
class IOController @Inject()(val controllerComponents: ControllerComponents,
                             dataStore: DataStore,
                             val rc210Actor: ActorRef[RC210Actor.RC210Message]
                            ) extends BaseController {
  implicit val timeout: Timeout = 5.seconds

  def downloadJson(): Action[AnyContent] = Action {

    val jsObject = Json.toJson(dataStore)
    val sJson = Json.prettyPrint(jsObject)

    Ok(sJson).withHeaders(
      "Content-Type" -> "text/json",
      "Content-Disposition" -> s"""attachment; filename="rc210.json""""
    )
  }

  def download(serialPortDescriptor: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      rc210Actor ! StartDownload(serialPortDescriptor)
      Ok(views.html.RC210DownloadProgress())
//    val triedInts: Try[Array[Int]] = RC210Download.download(serialPortDescriptor)
//    triedInts match {
//      case Failure(exception) =>
//        InternalServerError(exception.getMessage)
//      case Success(value: Array[Int]) =>
//        Ok(views.html.RC210DownloadProgress
//
//    }


  }

  def listSerialPorts(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
    val ports: List[ComPort] = RC210Download.listPorts
    val rows = ports.sortBy(_.friendlyName)
      .filterNot(_.descriptor.contains("/dev/tty")) // thedse are just clutter.
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


  import play.api.mvc._
  import akka.stream.scaladsl._

  def socket: WebSocket = WebSocket.accept[String, String] { request =>
    // Log events to the console
    val in = Sink.foreach[String](mess =>
        println(mess))

    // Send a single 'Hello!' message and then leave the socket open
    val out = Source.single("Hello!").concat(Source.maybe)

    Flow.fromSinkAndSource(in, out)
  }

}
