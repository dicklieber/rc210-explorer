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
import net.wa9nnn.rc210.serial.comm.{BatchOperationsResult, Rc210, SerialPortsSource}
import net.wa9nnn.rc210.serial.{ComPort, CurrentSerialPort, RcSerialPortManager}
import play.api.mvc._

import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Try

@Singleton
class IOController @Inject()(implicit val controllerComponents: ControllerComponents,
                             currentSerialPort: CurrentSerialPort,
                             rcSerialPortManager: RcSerialPortManager,
                             rc210: Rc210,
                            ) extends BaseController with LazyLogging {
  implicit val timeout: Timeout = 5.seconds

  def downloadResult: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      throw new NotImplementedError() //todo
  }


  def select(descriptor: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      rcSerialPortManager.selectPort(descriptor)
      Redirect(routes.IOController.listSerialPorts)
  }


  def listSerialPorts: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val table = currentSerialPort.table(rc210)
      Ok(views.html.RC210Landings(table))
  }
}


