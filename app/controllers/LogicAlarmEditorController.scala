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
import net.wa9nnn.rc210.data.datastore.{AllForKeyKind, DataStoreActor, DataStoreApi, DataStoreMessage, DataStoreReply}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.logicAlarm.LogicAlarm
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.{LogRequest, ProcessResult}
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class LogicAlarmEditorController @Inject()(components: MessagesControllerComponents, dataStore: DataStoreApi)
  extends MessagesAbstractController(components) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index(): Action[AnyContent] = Action.async {
    implicit request =>
      val functionToEventualResult: Future[Result] = dataStore.indexValues[LogicAlarm](KeyKind.logicAlarmKey,
        values => Ok(views.html.logic(values)))
      functionToEventualResult
  }

  def edit(logicAlarmKey: Key): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("LogicAlarm", logicAlarmKey)
      dataStore.editOne[LogicAlarm](fieldKey, logicAlarm =>
        val form: Form[LogicAlarm] = LogicAlarm.logicForm.fill(logicAlarm)
        Ok(views.html.logicEditor(form, logicAlarmKey.namedKey))
      )
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    LogRequest(logger)

    logicForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[LogicAlarm]) => {
          val namedKey = Key(formWithErrors.data("key")).namedKey
          Future(BadRequest(views.html.logicEditor(formWithErrors, namedKey)))
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