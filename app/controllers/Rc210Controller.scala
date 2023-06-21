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

import akka.actor.typed.Scheduler
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Table}
import net.wa9nnn.rc210.serial.ComPortPersistence
import net.wa9nnn.rc210.serial.comm.{BatchOperationsResult, Rc210}
import play.api.mvc._

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

@Singleton
class Rc210Controller @Inject()(comPortPersistence: ComPortPersistence, rc210: Rc210)(implicit scheduler: Scheduler, ec: ExecutionContext, mat: Materializer)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  //  *21999

  private var lastResults: Option[BatchOperationsResult] = None //todo this belongs in the users session.

  def lastResult: Action[AnyContent] = Action { implicit request =>

    val table = lastResults match {
      case Some(opResult: BatchOperationsResult) =>
        Table(Header(), opResult.toRows)
      case None =>
        Table(Seq.empty,  Seq.empty)
    }

    lastResults = None // just shown once.

     Ok(views.html.lastResults(table))
  }

  def setClock(): Action[AnyContent] = Action { implicit request =>

    //    serialPortsActor
    //   clock:  *5100 12 12 23
    // calander:   *5101 06 11 03 Set June 11, 2003 as the current date
    val dt = LocalDateTime.now()
    val clock = s"1*5100${dt.getHour}${dt.getMinute}${dt.getSecond}"
    val calendar = s"1*5101${dt.getMonthValue}${dt.getDayOfMonth}${dt.getYear - 2000}"

    rc210.send("SetClock", clock, calendar) match {
      case Failure(exception) =>
        throw exception
      case Success(operationsResult: BatchOperationsResult) =>
        lastResults = Option(operationsResult)
        Redirect(routes.IOController.listSerialPorts.url)
    }
  }

  def restart(): Action[AnyContent] = Action { implicit request =>
    rc210.send("SetClock", "1*21999") match {
      case Failure(exception) =>
        throw exception
      case Success(operationsResult: BatchOperationsResult) =>
        lastResults = Option(operationsResult)
        Redirect(routes.IOController.listSerialPorts.url)
    }
  }

}
