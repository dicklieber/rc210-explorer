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

import akka.stream.{Materializer, OverflowStrategy}
import com.google.common.util.concurrent.AtomicDouble
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import controllers.CandidateController.performInit
import net.wa9nnn.rc210.data.clock.{Clock, DSTPoint}
import net.wa9nnn.rc210.data.{FieldKey}
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.serial.{CommandTransaction, RC210IO, SerialPortOpenException, SerialPortOperation}
import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, number}
import play.api.mvc._

import java.io.PrintWriter
import java.nio.file.Files
import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}
import scala.util.{Try, Using}

@Singleton()
class ClockController @Inject()(dataStore: DataStore) extends MessagesInjectedController with LazyLogging {

  def index: Action[AnyContent] = Action { implicit request =>
    val fieldEntry: FieldEntry = dataStore(KeyKind.clockKey).head

    Ok(views.html.clock(fieldEntry.fieldValue.asInstanceOf[Clock]))
  }

  def setClock:Action[AnyContent]= Action { implicit request =>


    Ok("todo st clock in RC-210")
  }

}



