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
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, UpdateData}
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.field.Formatters.*
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.data.timers.Timer
import net.wa9nnn.rc210.key.{MacroKey, TimerKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.util.Timeout
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class TimerEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                     (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  //  private val timerForm: Form[Timer] = Form[Timer](
  //    mapping(
  //      "timerKey" -> of[TimerKey],
  //      "seconds" -> number,
  //      "macroKey" -> of[MacroKey],
  //    )(Timer.apply)(Timer.unapply)
  //  )

  def index: Action[AnyContent] = Action {
    //    implicit request: Request[AnyContent] =>

    //      actor.ask(AllForKeyKind(KeyKind.timerKey, _)).map { (timerFields: Seq[FieldEntry]) =>
    //        val timers: Seq[Timer] = timerFields.map {
    //          _.value
    //        }
    ImATeapot
    //        Ok(views.html.timers(timers))
  }

  def edit(timerKey: TimerKey): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("Timer", timerKey)
      actor.ask(DataStoreActor.ForFieldKey(fieldKey, _)).map {
        case Some(fieldEntry: FieldEntry) =>
          Ok(views.html.timerEditor(fieldEntry))
        case None =>
          NotFound(s"No timer: $timerKey")
      }
  }

  def save(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>

      ImATeapot
    //      val fields: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
    //      val name: String = fields("name").head
    //      val timerKey: TimerKey = KeyFactory.apply(fields("timerKey").head)
    //
    //      timerForm.bindFromRequest().fold(
    //        formWithErrors => {
    //          Future(BadRequest(views.html.timerEditor(formWithErrors, timerKey)))
    //        },
    //        (timer: Timer) => {
    //          val updateCandidate: UpdateCandidate = UpdateCandidate(timer.fieldKey, Right(timer))
    //          actor.ask[String](UpdateData(Seq(updateCandidate), Seq(NamedKey(timerKey, name)), who(request), _)).map { _ =>
    //            Redirect(routes.TimerEditorController.index)
    //          }
    //        }
    //      )

    //  }
  }
}

case class Timers(timers: Seq[Timer])