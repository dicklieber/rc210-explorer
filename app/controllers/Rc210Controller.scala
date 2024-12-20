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

//import akka.stream.Materializer
//import akka.util.Timeout

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.DurationHelpers
import com.wa9nnn.wa9nnnutil.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.serial.{DataCollector, Rc210}
import net.wa9nnn.rc210.ui.TabE
import org.apache.pekko.util.Timeout
import play.api.mvc.MessagesAbstractController
import views.html.{NavMain, justdat}

import java.time.Instant
import scala.concurrent.duration.Duration
//import configs.syntax._
import play.api.mvc.*

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class Rc210Controller @Inject()(rc210: Rc210, dataCollector: DataCollector,
                                navMain: NavMain,
                                config: Config, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def setClock(): Action[AnyContent] = Action { implicit request =>
    val dt = LocalDateTime.now()
    val clock = s"1*5100${dt.getHour}${dt.getMinute}${dt.getSecond}"
    val calendar = s"1*5101${dt.getMonthValue}${dt.getDayOfMonth}${dt.getYear - 2000}"

    val operationsResult = rc210.sendBatch(clock, calendar)
    Redirect(routes.IOController.listSerialPorts.url)
  }

  def restart(): Action[AnyContent] = Action { implicit request =>
    val start: Instant = Instant.now()
    val batchOperationsResult = rc210.sendBatch("1*21999")
    val rows = batchOperationsResult.head.lines.map(line => Row("Line" -> line))
    val table = Table(Header("Startup", "Response"),
      rows.appended(Row("Startup in" -> DurationHelpers.between(start)))
    )
    Ok(navMain(TabE.Restart, justdat(Seq(table))))
  }

  /**
   * Start a download from RC210 operation.
   * All the work actually happens in [[ws]] and ultimately in [[DataCollector]].
   *
   * @return
   */
}
