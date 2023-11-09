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
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, UpdateData}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.key.KeyFactory.PortKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import net.wa9nnn.rc210.ui.{CandidateAndNames, FormParser}
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class PortsEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                     (implicit scheduler: Scheduler, ec: ExecutionContext) extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      actor ? (AllForKeyKind(KeyKind.portKey, _)).map { portEntries =>
        val map: Map[FieldKey, FieldEntry] = portEntries
          .map(fieldEntry => fieldEntry.fieldKey -> fieldEntry).toMap
        val fieldNames: Seq[String] = portEntries.foldLeft(Set.empty[String]) { case (set: Set[String], fieldEntry) =>
          set + fieldEntry.fieldKey.fieldName
        }.toSeq.sorted

        val rows: Seq[Row] = for {
          fieldName <- fieldNames
        } yield {
          val cells: Seq[Cell] = for {
            number <- 1 to KeyKind.portKey.maxN()
          } yield {
            map(FieldKey(fieldName, KeyFactory(KeyKind.portKey, number))).toCell
          }

          Row(fieldName, cells: _*)
        }

        val colHeaders: Seq[Cell] = for {
          portKey <- KeyFactory[PortKey](KeyKind.portKey)
        } yield {
          portKey.namedCell()
        }
        val namesRow = Row(colHeaders.prepended(Cell("Ports:").withCssClass("cornerCell")))

        val table = Table(Seq.empty, rows.prepended(namesRow))

        Ok(views.html.ports(table))
      }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      val candidateAndNames: CandidateAndNames = FormParser(AnyContentAsFormUrlEncoded(request.body.asFormUrlEncoded.get))

      actor.ask[String]((ref) => UpdateData(candidateAndNames, user=who(request), replyTo = ref)).map { _ =>
        Redirect(routes.PortsEditorController.index())
      }
  }
}
