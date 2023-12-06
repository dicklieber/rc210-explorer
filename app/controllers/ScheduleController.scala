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

import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKey, AllForKeyKind, ForFieldKey, UpdateData}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.message.Message
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.ProcessResult
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton()
class ScheduleController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                  (implicit scheduler: Scheduler, ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components)
    with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index: Action[AnyContent] = Action.async { implicit request =>
    actor.ask[Seq[FieldEntry]](AllForKeyKind(KeyKind.scheduleKey, _)).map { f =>
      val messages: Seq[Schedule] = f.map { fieldEntry =>
        fieldEntry.value.asInstanceOf[Schedule]
      }
      Ok(views.html.schedules(messages))
    }
  }

  def edit(key: Key): Action[AnyContent] = Action.async { implicit request =>
    val fieldKey = Schedule.fieldKey(key)
    actor.ask[Option[FieldEntry]](ForFieldKey(fieldKey, _)).map { (f: Option[FieldEntry]) => {
      val head: Schedule = f.get.value.asInstanceOf[Schedule]
      Ok(views.html.scheduleEdit(Schedule.form.fill(head), key.namedKey))
    }
    }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request =>
      Schedule.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[Schedule]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey
            Future(BadRequest(views.html.scheduleEdit(formWithErrors, namedKey)))
          },
          (schedule: Schedule) => {
            val candidateAndNames = ProcessResult(schedule)
            actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
              Redirect(routes.ScheduleController.index)
            }
          }
        )
  }
}
