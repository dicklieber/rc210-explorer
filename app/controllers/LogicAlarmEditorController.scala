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
import net.wa9nnn.rc210.Key.{keyFormatter, nameForKey}
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{ComplexExtractor, FieldEntry}
import net.wa9nnn.rc210.data.logicAlarm.LogicAlarm
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.Who.{*, given}
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.{ComplexFieldController, LogRequest, ProcessResult}
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*
import net.wa9nnn.rc210.security.Who.given

import javax.inject.*

@Singleton
class LogicAlarmEditorController @Inject()(components: MessagesControllerComponents, dataStore: DataStore)
  extends ComplexFieldController[LogicAlarm](dataStore, components) with LazyLogging {

  //  def index(): Action[AnyContent] = Action {
  //    implicit request: MessagesRequest[AnyContent] =>
  //      val logicAlarms: Seq[LogicAlarm] = dataStore.values(KeyKind.logicAlarmKey)
  //      val r: Result = Ok(views.html.logic(logicAlarms))
  //      r
  //  }
  //
  //  def edit(logicAlarmKey: Key): Action[AnyContent] = Action {
  //    implicit request: MessagesRequest[AnyContent] =>
  //      val fieldKey = FieldKey("LogicAlarm", logicAlarmKey)
  //      val logicAlarm: LogicAlarm = dataStore.editValue(fieldKey)
  //      val form: Form[LogicAlarm] = LogicAlarm.logicForm.fill(logicAlarm)
  //      Ok(views.html.logicEditor(form, logicAlarmKey.namedKey))
  //  }
  //
  //  def save(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
  //
  //    LogicAlarm.logicForm
  //      .bindFromRequest()
  //      .fold(
  //        (formWithErrors: Form[LogicAlarm]) => {
  //          val namedKey = Key(formWithErrors.data("key")).namedKey
  //          BadRequest(views.html.logicEditor(formWithErrors, namedKey))
  //        },
  //        (logicAlarm: LogicAlarm) => {
  //          val candidateAndNames = ProcessResult(logicAlarm)
  //          dataStore.update(candidateAndNames)
  //          Redirect(routes.LogicAlarmEditorController.index())
  //        }
  //      )
  //  }
  override val form: Form[LogicAlarm] = LogicAlarm.logicForm
  override val complexExtractor: ComplexExtractor = LogicAlarm

  override def indexResult(values: Seq[LogicAlarm]): Result = {
    val result: Result = Ok(views.html.logic(values))
    result
  }

  override def editResult(filledForm: Form[LogicAlarm], namedKey: NamedKey)(using request: MessagesRequest[AnyContent]): Result = {
    val result = Ok(views.html.logicEditor(form, namedKey))
    result
  }

  override def saveOkResult(): Result = Redirect(routes.LogicAlarmEditorController.index())
    
}

