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
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.serial.*
import net.wa9nnn.rc210.serial.comm.RcResponse
import net.wa9nnn.rc210.ui.{TabE, Tabs}
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
          val fieldKey = fieldEntry.fieldKey
          val row = Row(
            fieldKey.editButtonCell,
            fieldKey.toString,
            fieldEntry.fieldValue.displayCell,
            fieldEntry.value[FieldValue].displayCell,
            commandsCell(fieldEntry.commands)
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

  def dump(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldEntries = dataStore.candidates
      Ok(views.html.dump(fieldEntries))
  }



  //  /**
  //   * Send command for one [[FieldKey]] to the RC210.
  //   *
  //   * @param fieldKeyStuff  to send
  //   * @param sendValue true to send the fieldValue's command. false to send and accept the candidate's.
  //   * @return
  //   */
  //  def send(fieldKeyStuff: FieldKey, sendValue: Boolean = false): Action[AnyContent] = Action {
  //    implicit request =>
  ///*      val fieldEntry: FieldEntry = dataStore(fieldKeyStuff)
  //      val commands: Seq[String] = fieldEntry
  //        .candidate
  //        .get
  //        .toCommands(fieldEntry)
  //      val batchOperationsResult: BatchOperationsResult = rc210.sendBatch(fieldKeyStuff.toString, commands: _*)
  //      Ok(batchOpResult(batchOperationsResult))
  //*/
  //    NotImplemented("todo")
  //  }

  def ws(commandSendRequest: CommandSendRequest): WebSocket =
    //todo handle authorization See https://www.playframework.com/documentation/3.0.x/ScalaWebSockets

    new ProcessWithProgress[RcResponse](1)(
      progressApi =>
        commandsSender(commandSendRequest, progressApi)
    ).webSocket

  def socket: WebSocket = WebSocket.accept[String, String] { request =>
    // log the message to stdout and send response back to client
    Flow[String].map { msg =>
      println(msg)
      "I received your message: " + msg
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
