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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.Rc210
import net.wa9nnn.rc210.ui.TabE.SerialPort
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import views.html.NavMain

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class IOController @Inject()(implicit val controllerComponents: ControllerComponents,
                             rc210: Rc210, navMain: NavMain
                            ) extends BaseController with LazyLogging {
  implicit val timeout: Timeout = 5.seconds

  def downloadResult: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      throw new NotImplementedError() //todo
  }

  def select(descriptor: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      rc210.selectPort(descriptor)
      Redirect(routes.IOController.listSerialPorts)
  }

  def listSerialPorts: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(navMain(SerialPort, views.html.RC210SerialPorts(rc210)))
  }
}


