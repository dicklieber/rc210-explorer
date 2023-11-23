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

import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Row, Table}
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class PortsEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                     (implicit scheduler: Scheduler, ec: ExecutionContext) extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def buildresult(portEntries: Seq[FieldEntry]): Result =
    val key2EntryMap: Map[FieldKey, FieldEntry] = portEntries.map(fieldEntry => fieldEntry.fieldKey -> fieldEntry).toMap

    val fieldNames: Seq[String] = key2EntryMap.values.map(_.fieldKey.fieldName).toSeq // todo sorted

    val rows: Seq[Row] = fieldNames.map { fieldName =>
      val cells: Seq[Cell] = for
        number <- 1 to KeyKind.portKey.maxN
      yield
        key2EntryMap(FieldKey(fieldName, Key(KeyKind.portKey, number))).toCell
      Row(fieldName, cells: _*)
    }

    val colHeaders: Seq[Cell] = Key.portKeys.map(key =>Cell(key.keyWithName))
    val namesRow = Row(colHeaders.prepended(Cell("Ports:").withCssClass("cornerCell")))

    val table = Table(Seq.empty, rows.prepended(namesRow))
    Ok(views.html.ports(table))

  def index(): Action[AnyContent] = Action.async {
    implicit rquest: MessagesRequest[AnyContent] => {
      val future: Future[Seq[FieldEntry]] = actor.ask(AllForKeyKind(KeyKind.portKey, _))
      future.map { (portEntries: Seq[FieldEntry]) =>
        buildresult(portEntries)
      }
    }
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      ImATeapot
    //      val candidateAndNames: CandidateAndNames = FormParser()
    //
    //      actor.ask[String](ref => UpdateData(candidateAndNames, user = user(request), replyTo = ref)).map { _ =>
    //        Redirect(routes.PortsEditorController.index())
    //      }
  }
}
