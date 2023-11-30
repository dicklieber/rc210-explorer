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
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.{CandidateAndNames}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class CommonEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                      (implicit scheduler: Scheduler, ec: ExecutionContext, cc: MessagesControllerComponents)
  extends AbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private var fieldKeys: Seq[FieldKey] = Seq.empty

  def index(): Action[AnyContent] = Action.async {
    implicit request =>
      val eventualFieldEntries: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKeyKind(KeyKind.commonKey, _))

      eventualFieldEntries.map { fieldEntries =>
        if (fieldKeys.isEmpty)
          fieldKeys = fieldEntries.map(_.fieldKey)

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

  def save(): Action[AnyContent] = Action.async {
    Future(ImATeapot("todo"))
/*    implicit request: Request[AnyContent] =>

      val data = new FormExtractor(request)

      val uc: Seq[UpdateCandidate] = fieldKeys.map(fieldKey =>
        val str: String = data.stringOpt(fieldKey.toString).getOrElse("")
        UpdateCandidate(fieldKey, Left(str)) //todo how does this work with bool
      )
      logger.whenTraceEnabled {
        uc.foreach { uc =>
          logger.trace(uc.toString)
        }
      }

      actor.ask[String](DataStoreActor.UpdateData(uc, Seq.empty, user, _)).map { _ =>
        Redirect(routes.CommonEditorController.index())
      }
*/  }
}
