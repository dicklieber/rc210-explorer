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
import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.field.FieldEntry
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class CommonEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                      (implicit scheduler: Scheduler, ec: ExecutionContext, cc: MessagesControllerComponents)
  extends AbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index(): Action[AnyContent] = Action.async {
    implicit request =>
      val eventualFieldEntries: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKeyKind(KeyKind.commonKey, _))

      eventualFieldEntries.map { fieldEntries =>
        val rows: Seq[Row] = fieldEntries.map { fieldEntry =>
          //          // Can't use fieldEntry's toRow because we just want the rc2input name not key, as they are all commonKey1
          Row(
            fieldEntry.fieldKey.fieldName,
            fieldEntry.toCell
          )

        }
        val header = Header(s"Common (${rows.length} values)", "Field", "Value")
        val table = Table(header, rows)
        Ok(views.html.common(table))


      }

    //        .map { (commonFields: Seq[FieldEntry]) =>
    //        commonFields.map { fieldEntry =>
    //          // Can't use fieldEntry's toRow because we just want the rc2input name not key, as they are all commonKey1
    //          Row(
    //            fieldEntry.fieldKey.fieldName,
    //            fieldEntry.toCell
    //          )
    //        }
    //      }
    //      eventualRows.map { rows =>
    //        val header = Header(s"Common (${rows.length} values)", "Field", "Value")
    //        val table = Table(header, rows)
    //        Ok(views.html.common(table))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
                ImATeapot
//      val updateData: CandidateAndNames = FormParser(AnyContentAsFormUrlEncoded(request.body.asFormUrlEncoded.get))
//
//      actor.ask[String](DataStoreActor.UpdateData(updateData, user, _)).map { _ =>
//        Redirect(routes.CommonEditorController.index())
//      }
  }
}
