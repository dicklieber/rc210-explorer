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
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, UpdateData}
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.field.Formatters._
import net.wa9nnn.rc210.data.timers.Timer
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, TimerKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import javax.inject._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class TimerEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                     (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  private val timerForm: Form[Timer] = Form[Timer](
    mapping(
      "timerKey" -> of[TimerKey],
      "seconds" -> number,
      "macroKey" -> of[MacroKey],
    )(Timer.apply)(Timer.unapply)
  )

  def index(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      actor.ask(AllForKeyKind(KeyKind.timerKey, _)).map { timerFields: Seq[FieldEntry] =>
        val timers: Seq[Timer] = timerFields.map {fieldEntry =>
          val fieldValue: Timer = fieldEntry.value
          fieldValue.asInstanceOf[Timer]
        }
        Ok(views.html.timers(timers))
      }
  }

  def edit(timerKey: TimerKey): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("Timer", timerKey)
      actor.ask(DataStoreActor.ForFieldKey(fieldKey, _)).map {
        case Some(fieldEntry: FieldEntry) =>
          Ok(views.html.timerEditor(timerForm.fill(fieldEntry.value), timerKey: TimerKey))
        case None =>
          NotFound(s"No timer: $timerKey")
      }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>

      val fields: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
      val name: String = fields("name").head
      val timerKey: TimerKey = KeyFactory(fields("timerKey").head)

      timerForm.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.timerEditor(formWithErrors, timerKey)))
        },
        (timer: Timer) => {
          val updateCandidate: UpdateCandidate = UpdateCandidate(timer.fieldKey, Right(timer))
          actor.ask[String](UpdateData(Seq(updateCandidate), Seq.empty, who(request), _)).map { _ =>
            Redirect(routes.ClockController.index)
          }
        }
      )

  }
}

case class Timers(timers: Seq[Timer])