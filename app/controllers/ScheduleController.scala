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
import com.wa9nnn.wa9nnnutil.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.schedules.ScheduleNode
import net.wa9nnn.rc210.security.Who.request2Session
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton()
class ScheduleController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {

  def index: Action[AnyContent] = Action { implicit request =>
    val schedules: Seq[ScheduleNode] = dataStore.indexValues(KeyKind.Schedule)
    Ok(views.html.schedules(schedules))
  }

  def edit(key: Key): Action[AnyContent] = Action { implicit request =>
    val fieldKey = ScheduleNode.fieldKey(key)
    val schedule: ScheduleNode = dataStore.editValue(fieldKey)
    Ok(views.html.scheduleEdit(ScheduleNode.form.fill(schedule), key.namedKey))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      ScheduleNode.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[ScheduleNode]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey
            BadRequest(views.html.scheduleEdit(formWithErrors, namedKey))
          },
          (schedule: ScheduleNode) => {
            val candidateAndNames = ProcessResult(schedule)

            given RcSession = request.attrs(sessionKey)

            dataStore.update(candidateAndNames)
            Redirect(routes.ScheduleController.index)
          }
        )
  }
}
