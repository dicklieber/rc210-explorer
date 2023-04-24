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
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.serial.{CommandTransaction, RC210IO}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton()
class CandidateController @Inject()(dataStore: DataStore) extends MessagesInjectedController  with LazyLogging{
  def index(): Action[AnyContent] = Action { implicit request =>
    val candidates = dataStore.candidates
    val rows: Seq[Row] =
      candidates.map { fieldEntry =>
        val candidate = fieldEntry.candidate.get
        Row(fieldEntry.fieldKey.key.toCell, fieldEntry.fieldKey.fieldName, fieldEntry.fieldValue.display, candidate.display, fieldEntry.toCommands)
      }
    val table = Table(Header(s"Candidates (${candidates.length})", "Key", "Field", "Was", "New", "Command"), rows)

    Ok(views.html.candidates(table))
  }

  def dump(): Action[AnyContent] = Action {

    val rows: Seq[Row] = dataStore
      .all
      .map { fieldEntry =>
        val value: FieldValue = fieldEntry.fieldValue
        val fieldKey = fieldEntry.fieldKey
        val key = fieldKey.key
        val commands = value.toCommands(fieldEntry)
        val sendValueButton = Cell(commands.mkString("</br>")).withUrl(routes.CandidateController.sendCandidate(fieldKey).url)
        var row = Row(key.toString, fieldKey.fieldName, value.display, sendValueButton)

        fieldEntry.candidate.foreach { candidateValue =>
          row = row :+ candidateValue.display
          val commands = candidateValue.toCommands(fieldEntry)
          val sendValueButton = Cell(commands.mkString("</br>")).withUrl(routes.CandidateController.sendCandidate(fieldKey).url)
          row = row :+ sendValueButton
        }
        row
      }
    val header = Header(s"All entries (${rows.length})", "Key", "Field Name", "Field Value", "Command", "Candidate Value", "Candidate Command")
    val table = Table(header, rows)
    Ok(views.html.dat(Seq(table)))
  }


  def sendFieldValue(fieldKey: FieldKey): Action[AnyContent] = {
    send(fieldKey, sendValue = true)
  }

  def sendCandidate(fieldKey: FieldKey): Action[AnyContent] = {
    send(fieldKey)
  }

  /**
   * Send command to the RC-210.
   *
   * @param fieldKey
   * @param sendValue true to send the fieldValue's command. false to send and accept the candidate's.
   * @return
   */
  def send(fieldKey: FieldKey, sendValue: Boolean = false): Action[AnyContent] = Action { implicit request =>
    dataStore(fieldKey) match {
      case Some(fieldEntry: FieldEntry) =>

        val commands: Seq[String] = fieldEntry.fieldValue.toCommands(fieldEntry)
      val transactions: Seq[CommandTransaction] =   commands.map{ command =>
          val withCr = command + "\r"
          val triedResponse: Try[String] = RC210IO.sendReceive(withCr)
          val transaction = CommandTransaction(withCr, fieldEntry, triedResponse)
          logger.debug(transaction.toString)
          transaction
        }
        Ok(transactions.map(_.toString).mkString("\n"))
      case None =>
        NotFound(s"No fieldKey: $fieldKey")
    }
  }


}

