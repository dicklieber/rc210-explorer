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
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, UpdateData}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.timers.Timer
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.data.Form
import play.api.mvc.*
import views.html.timerEditor

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class TimerController @Inject()(actor: ActorRef[DataStoreActor.Message])
                               (implicit scheduler: Scheduler, ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds


  def index: Action[AnyContent] = Action.async {
    implicit request =>

      val future: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKeyKind(KeyKind.timerKey, _))
      future.map { (fieldEntries: Seq[FieldEntry]) => {
        val timers: Seq[Timer] = fieldEntries.map(fe => fe.value.asInstanceOf[Timer])
        Ok(views.html.timers(timers))
      }
      }
  }

  def edit(key: Key): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("Timer", key)
      actor.ask(DataStoreActor.ForFieldKey(fieldKey, _)).map {
        case Some(fieldEntry: FieldEntry) =>
          val form: Form[Timer] = Timer.form.fill(fieldEntry.value)
          Ok(timerEditor(form, key.namedKey))
        case None =>
          NotFound(s"No timer: $key")
      }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request =>
      Timer.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[Timer]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey
            Future(BadRequest(timerEditor(formWithErrors, namedKey)))
          },
          (schedule: Timer) => {
            val candidateAndNames = ProcessResult(schedule)
            actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
              Redirect(routes.TimerController.index)
            }
          }
        )
  }
}
