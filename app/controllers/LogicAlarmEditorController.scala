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
import net.wa9nnn.rc210.Key.keyFormatter
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, UpdateData}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.logicAlarm.LogicAlarm
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class LogicAlarmEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                          (implicit scheduler: Scheduler, ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private val logicForm: Form[LogicAlarm] = Form(
    mapping(
      "key" -> of[Key],
      "enable" -> boolean,
      "lowMacro" -> of[Key],
      "highMacro" -> of[Key]
    )(LogicAlarm.apply)(LogicAlarm.unapply)
  )


  def index(): Action[AnyContent] = Action.async {
    implicit request =>
      actor.ask[Seq[FieldEntry]](AllForKeyKind(KeyKind.logicAlarmKey, _)).map { alarmFields =>
        val logicAlarms: Seq[LogicAlarm] = alarmFields.map(_.value.asInstanceOf[LogicAlarm])
        Ok(views.html.logic(logicAlarms))
      }
  }

  def edit(logicAlarmKey: Key): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("LogicAlarm", logicAlarmKey)
      actor.ask[Option[FieldEntry]](DataStoreActor.ForFieldKey(fieldKey, _)).map {
        {
          case Some(fieldEntry) =>
            val logicAlarm: LogicAlarm = fieldEntry.value
            val form: Form[LogicAlarm] = logicForm.fill(logicAlarm)
            Ok(views.html.logicEditor(form))
          case None =>
            NotFound(s"No key: $logicAlarmKey")
        }
      }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    logicForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[LogicAlarm]) => {
          Future(BadRequest(views.html.logicEditor(formWithErrors)))
        },
        (logicAlarm: LogicAlarm) => {
          val candidateAndNames = ProcessResult(logicAlarm)
          actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
            Redirect(routes.LogicAlarmEditorController.index())
          }
        }
      )
  }
}