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

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table}
import controllers.CandidatesController.commandsCell
import io.jsonwebtoken.Jwts.header
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.serial.*
import net.wa9nnn.rc210.serial.comm.RcResponse
import net.wa9nnn.rc210.ui.{ButtonCell, TabE, Tabs}
import net.wa9nnn.rc210.util.Configs.path
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Flow
import play.api.libs.json.{Format, Json}
import play.api.mvc.*
import views.html.NavMain

import java.nio.file.Path
import javax.inject.{Inject, Singleton}
import scala.util.matching.Regex

/**
 * Sends commands to the RC-210.
 */
@Singleton()
class CandidatesController @Inject()(dataStore: DataStore,
                                     commandsSender: CommandsSender,
                                     rc210: Rc210, navMain: NavMain)
                                    (implicit config: Config, mat: Materializer, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {
  private val sendLogFile: Path = path("vizRc210.sendLog")

  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] => {
      val fieldEntries: Seq[FieldEntry] = dataStore.candidates
      val rows: Seq[Row] = fieldEntries.map {
        fieldEntry =>
          val key: Key = fieldEntry.key
          val fieldData = fieldEntry.fieldData
          val row = Row(
            ButtonCell.edit(key),
            key.display,
            fieldData.fieldValue.displayCell,
            fieldData.candidate.map(_.displayCell).getOrElse(""),
            commandsCell(fieldEntry.toCommands)
          )
          row
      }
      val table = Table(
        CandidatesController.header(fieldEntries.length),
        rows
      )

      Ok(navMain(TabE.Changes, views.html.candidates(table)))
    }
  }
}

object CandidatesController:
  def commandsCell(commands: Seq[String]): Cell =
    val x = (<ul class="commandsUl">
      {commands.map { cmd =>
        <li>
          {cmd}
        </li>
      }}
    </ul>)
    val string = x.toString
    Cell
      .rawHtml(string)
      .withCssClass("commandsUl")

  def header(count: Int): Header =
    Header(s"Candidate Changes ($count)",
      "",
      "Key",
      "Was",
      "New",
      "Commands",
    )

