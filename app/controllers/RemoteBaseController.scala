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
import net.wa9nnn.rc210.data.datastore.{CandidateAndNames, DataStore, UpdateCandidate}
import net.wa9nnn.rc210.data.remotebase.RemoteBaseNode
import play.api.data.Forms.*
import play.api.data.{Field, Form, Mapping}
import play.api.mvc.*
import net.wa9nnn.rc210.security.Who.request2Session
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey

import javax.inject.{Inject, Singleton}

@Singleton()
class RemoteBaseController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {

  def index: Action[AnyContent] = Action {
    implicit request =>
      val remoteBase: RemoteBaseNode = dataStore.editValue(RemoteBaseNode.fieldKey)
      val value: Form[RemoteBaseNode] = RemoteBaseNode.form.fill(remoteBase)
      Ok(views.html.remoteBase(value))
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    RemoteBaseNode.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RemoteBaseNode]) => {
          BadRequest(views.html.remoteBase(formWithErrors))
        },
        (remoteBase: RemoteBaseNode) => {
          val updateCandidate = UpdateCandidate(RemoteBaseNode.fieldKey, remoteBase)
          val candidateAndNames = CandidateAndNames(updateCandidate, None)

          given RcSession = request.attrs(sessionKey)

          dataStore.update(candidateAndNames)
          Redirect(routes.RemoteBaseController.index)
        }
      )
  }
}



