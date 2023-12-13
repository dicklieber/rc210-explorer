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
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.data.datastore.{CandidateAndNames, DataStore, UpdateCandidate}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.remotebase.RemoteBase
import net.wa9nnn.rc210.data.timers.Timer
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.ProcessResult
import play.api.data.Forms.*
import play.api.data.{Field, Form, Mapping}
import play.api.mvc.*

import javax.inject.{Inject, Singleton}

@Singleton()
class RemoteBaseController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {

  def index: Action[AnyContent] = Action {
    implicit request =>
      val remoteBase: RemoteBase = dataStore.editValue(RemoteBase.fieldKey)
      val value: Form[RemoteBase] = RemoteBase.form.fill(remoteBase)
      Ok(views.html.remoteBase(value))
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    RemoteBase.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RemoteBase]) => {
          BadRequest(views.html.remoteBase(formWithErrors))
        },
        (remoteBase: RemoteBase) => {
          val updateCandidate = UpdateCandidate(RemoteBase.fieldKey, Right(remoteBase))
          dataStore.update(CandidateAndNames(updateCandidate, None))
          Redirect(routes.RemoteBaseController.index)
        }
      )
  }
}



