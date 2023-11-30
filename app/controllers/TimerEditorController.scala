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
import net.wa9nnn.rc210.data.clock.Clock
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.AllForKeyKind
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.timers.Timer
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class TimerEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                     (implicit scheduler: Scheduler, ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  private val timerForm: Form[Timer] = Form[Timer](
    mapping(
      "timerKey" -> of[Key],
      "seconds" -> number,
      "macroKey" -> of[Key],
    )(Timer.apply)(Timer.unapply)
  )

//  def indexC: Action[AnyContent] = Action.async {
//    implicit request =>
//      actor.ask[Seq[FieldEntry]](DataStoreActor.AllForKeyKind(KeyKind.clockKey, _)).map { fieldEntries =>
//        val fieldEntry: FieldEntry = fieldEntries.head
//        val clock: Clock = fieldEntry.value.asInstanceOf[Clock]
//
//        Ok(views.html.clock(clockForm.fill(clock)))
//        Ok(views.html.clock(clockForm.fill(clock)))
//      }
//  }

  def index: Action[AnyContent] = Action.async {
    implicit request =>

      val future: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKeyKind(KeyKind.timerKey, _))
      future.map { (fieldEntries: Seq[FieldEntry]) =>
        val timers: Seq[Timer] = fieldEntries.map(fe => fe.value.asInstanceOf[Timer])
        Ok("todo")
//        Ok(views.html.timers(timers))
      }
  }

  def edit(timerKey: Key): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("Timer", timerKey)
      actor.ask(DataStoreActor.ForFieldKey(fieldKey, _)).map {
        case Some(fieldEntry: FieldEntry) =>
          val form = timerForm.fill(fieldEntry.value)
          Ok(views.html.timerEditor(form))
        case None =>
          NotFound(s"No timer: $timerKey")
      }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      Future(ImATeapot)
  }
}
