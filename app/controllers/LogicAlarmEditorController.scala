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

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Row, Table}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, UpdateData}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.key.KeyFactory.LogicAlarmKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import net.wa9nnn.rc210.ui.{CandidateAndNames, FormParser}
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class LogicAlarmEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                          (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index(): Action[AnyContent] = Action.async {
    implicit request =>
      actor.ask[Seq[FieldEntry]](AllForKeyKind(KeyKind.logicAlarmKey, _)).map { alarmFields =>
        val map: Map[FieldKey, FieldEntry] = alarmFields
          .map(fieldEntry => fieldEntry.fieldKey -> fieldEntry).
          toMap
        val fieldNames: Seq[String] = alarmFields.foldLeft(Set.empty[String]) { case (set: Set[String], fieldEntry) =>
          set + fieldEntry.fieldKey.fieldName
        }.toSeq
          .sorted

        val rows: Seq[Row] = for {
          fieldName <- fieldNames
        } yield {
          val cells: Seq[Cell] = for {
            number <- 1 to KeyKind.logicAlarmKey.maxN()
          } yield {
            val fieldKey = FieldKey(fieldName, KeyFactory(KeyKind.logicAlarmKey, number))
            val fieldEntry = map(fieldKey)
            fieldEntry.toCell
          }

          Row(fieldName, cells: _*)
        }

        val colHeaders: Seq[Cell] = for {
          portKey <- KeyFactory[LogicAlarmKey](KeyKind.logicAlarmKey)
        } yield {
          portKey.namedCell()
        }
        val namesRow: Row = Row(colHeaders.prepended(Cell("Alarm:").withCssClass("cornerCell")))
        val table = Table(Seq.empty, rows.prepended(namesRow))
        Ok(views.html.logic(table))
      }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      val candidateAndNames: CandidateAndNames = FormParser(AnyContentAsFormUrlEncoded(request.body.asFormUrlEncoded.get))
      actor.ask[String](UpdateData(candidateAndNames, user = who(request), _)).map { _ =>
        Redirect(routes.LogicAlarmEditorController.index())
      }
  }
}