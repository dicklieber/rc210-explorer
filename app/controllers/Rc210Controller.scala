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

import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Table}
import configs.syntax._
import net.wa9nnn.rc210.serial.comm.{BatchOperationsResult, DataCollector, ProcessWithProgress, Rc210}
import play.api.mvc._

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class Rc210Controller @Inject()(rc210: Rc210, dataCollector: DataCollector, config: Config)(implicit mat: Materializer)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private val expectedLines: Int = config.get[Int]("vizRc210.expectedRcLines").value

  //  *21999

  private var lastResults: Option[BatchOperationsResult] = None //todo this belongs in the users session.


  def lastResult: Action[AnyContent] = Action { implicit request =>
    val table = lastResults match {
      case Some(opResult: BatchOperationsResult) =>
        Table(Header(), opResult.toRows)
      case None =>
        Table(Seq.empty, Seq.empty)
    }

    lastResults = None // just shown once.

    Ok(views.html.lastResults(table))
  }

  def setClock(): Action[AnyContent] = Action { implicit request =>

    //    serialPortsActor
    //   clock:  *5100 12 12 23
    // calendar:   *5101 06 11 03 Set June 11, 2003 as the current date
    val dt = LocalDateTime.now()
    val clock = s"1*5100${dt.getHour}${dt.getMinute}${dt.getSecond}"
    val calendar = s"1*5101${dt.getMonthValue}${dt.getDayOfMonth}${dt.getYear - 2000}"

    val operationsResult = rc210.sendBatch("SetClock", clock, calendar)
    lastResults = Option(operationsResult)
    Redirect(routes.IOController.listSerialPorts.url)
  }


  def restart(): Action[AnyContent] = Action { implicit request =>
    val batchOperationsResult = rc210.sendBatch("Restart", "1*21999")
    lastResults = Option(batchOperationsResult)
    Redirect(routes.IOController.listSerialPorts.url)
  }

  /**
   * Start a download from RC210 operation.
   * All the work actually happens in [[ws]] and ultimately in [[DataCollector]].
   *
   * @return
   */
  def download(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val webSocketURL: String = controllers.routes.Rc210Controller.ws(expectedLines).webSocketURL() //todo all fields expected.
      Ok(views.html.progress("Download from RC210", webSocketURL))
  }

  def ws(expected: Int): WebSocket = {
    ProcessWithProgress(expected, None)(progressApi =>
      dataCollector(progressApi)
    )
  }
}
