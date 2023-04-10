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

import akka.NotUsed
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.{BoundedSourceQueue, Materializer}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.DataStore
import net.wa9nnn.rc210.serial.{ComPort, ERamCollector, RC210Download}
import net.wa9nnn.rc210.util.Progress
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class IOController @Inject()(implicit val controllerComponents: ControllerComponents,
                             dataStore: DataStore,
                             mat: Materializer,
                             executionContext: ExecutionContext
                            ) extends BaseController with LazyLogging {
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

      val ec = new ERamCollector(serialPortDescriptor, (p: Progress) => {
        logger.trace("Progress: {}", p.toString)
        queue.offer(p)
      }, 35)

      ec.start()
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

  import play.api.mvc.WebSocket.MessageFlowTransformer

  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[String, Progress]

  private val (queue: BoundedSourceQueue[Progress], source: Source[Progress, NotUsed]) = Source.queue[Progress](128)

    .toMat(BroadcastHub.sink(16))(Keep.both)
    .run()

  val s: Source[Progress, NotUsed] = source

  def socket: WebSocket = WebSocket.accept[String, Progress] { request: RequestHeader =>
    //     Log events to the console
    val in = Sink.foreach[String](mess =>
      println(mess))

    val value = Flow.fromSinkAndSource(in, s)

    value
  }

}