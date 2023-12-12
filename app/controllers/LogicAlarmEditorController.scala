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
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.logicAlarm.LogicAlarm
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.{LogRequest, ProcessResult}
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*

import javax.inject.*
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class LogicAlarmEditorController @Inject()(components: MessagesControllerComponents, dataStore: DataStore)
  extends MessagesAbstractController(components) with LazyLogging {

  def index(): Action[AnyContent] = Action {
    implicit request =>
      val logicAlarms: Seq[LogicAlarm] = dataStore.values(KeyKind.logicAlarmKey)
      Ok(views.html.logic(logicAlarms))
  }

  def edit(logicAlarmKey: Key): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("LogicAlarm", logicAlarmKey)
      val logicAlarm: LogicAlarm = dataStore.editValue(fieldKey)
      val form: Form[LogicAlarm] = LogicAlarm.logicForm.fill(logicAlarm)
      Ok(views.html.logicEditor(form, logicAlarmKey.namedKey))
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    LogicAlarm.logicForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[LogicAlarm]) => {
          val namedKey = Key(formWithErrors.data("key")).namedKey
          BadRequest(views.html.logicEditor(formWithErrors, namedKey))
        },
        (logicAlarm: LogicAlarm) => {
          val candidateAndNames = ProcessResult(logicAlarm)
          dataStore.update(candidateAndNames)
          Redirect(routes.LogicAlarmEditorController.index())
        }
      )
  }
}