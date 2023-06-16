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

import akka.actor.typed.{ActorRef, Scheduler}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.util.Timeout
import com.fazecast.jSerialComm.SerialPort
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.serial.ComPort
import net.wa9nnn.rc210.serial.comm.SerialPortsActor._
import net.wa9nnn.rc210.serial.comm.{DataCollectorActor, SerialPortsActor, SerialPortsSource}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class IOController @Inject()(implicit val controllerComponents: ControllerComponents,
                             executionContext: ExecutionContext,
                             dataStoreActor: ActorRef[DataStoreActor.Message],
                             scheduler: Scheduler,
                             dataCollectorActor: ActorRef[DataCollectorActor.Message],
                             serialPortsActor: ActorRef[Message]

                            ) extends BaseController with LazyLogging {
  implicit val timeout: Timeout = 5.seconds


  //  def progress(): Action[AnyContent] = Action {
  //    implicit request: Request[AnyContent] =>
  //      val sJson: String = eramCollector.map { eRamCollector =>
  //        val progress1 = eRamCollector.progress
  //        val jsObject = Json.toJson(progress1)
  //        Json.prettyPrint(jsObject)
  //      }.getOrElse("no eramCollector")
  //      Ok(sJson)
  //  }

  def downloadResult: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      throw new NotImplementedError() //todo
    /*
          eramCollector match {
            case Some(eramCollector: ERamCollector) =>
              val table: Table = eramCollector.resultStatus.toTable
              Ok(views.html.RC210DownloadLandings(table))
            case None =>
              Ok("DownloadActor not performed!")
          }
    */
  }

  def download(descriptor: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      dataCollectorActor ! DataCollectorActor.StartDownload(descriptor)
      Ok(views.html.RC210DownloadProgress())
  }
  def select(descriptor: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      serialPortsActor ! SelectPort(descriptor)
      Redirect(routes.IOController.listSerialPorts())
  }

  def listSerialPorts(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>

      val current: Option[ComPort] = Await.result[Option[ComPort]](serialPortsActor.ask(CurrentPort), 10 seconds)

      serialPortsActor.ask(SerialPorts).map { comPorts =>
        val table = Table(Header("Serial Ports", "Descriptor", "Friendly Name"), comPorts.map { comPort =>
          val row: Row = Row(Cell(comPort.descriptor)
            .withUrl(routes.IOController.select(comPort.descriptor).url), comPort.friendlyName)
          if (current.contains(comPort))
            row.withCssClass("selected")
          else
            row
        })

        Ok(views.html.RC210DownloadLandings(table))
      }
  }

}

