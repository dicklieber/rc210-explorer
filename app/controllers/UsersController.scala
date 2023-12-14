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
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.timers.Timer
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.authentication.*
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.util.FormHelper
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.mvc.*
import play.api.mvc.Security.AuthenticatedRequest
import views.html.userEditor
import net.wa9nnn.rc210.security.Who.*

import javax.inject.{Inject, Singleton}
import scala.language.{implicitConversions, postfixOps}

/**
 * User Management
 */

/**
 * Handle create/edit/list users/
 */
@Singleton
class UsersController @Inject()(userStore: UserStore)(implicit cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {

  def users: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.users(userStore.users))
  }

  def editUser(id: UserId): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      userStore.get(id) match
        case Some(user: User) =>
          Ok(userEditor(UserEditDTO.form.fill(user.userEditDTO)))
        case None =>
          NotFound(s"UserId: $id not found!")
  }

  def addUser(): Action[AnyContent] = Action { implicit request =>
    Ok(userEditor(UserEditDTO.form.fill(UserEditDTO())))
  }

  def remove(userId: UserId): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      userStore.remove(userId)(session(request))
      Redirect(routes.UsersController.users())
  }

  def saveUser(): Action[AnyContent] = Action { 
    implicit request: MessagesRequest[AnyContent] =>
    UserEditDTO.form
      .bindFromRequest()
      .fold(
        formWithErrors => {
          BadRequest(userEditor(formWithErrors))
        },
        (editDTO: UserEditDTO) => {
          userStore.put(editDTO)(session(request))
          Redirect(routes.TimerController.index)
        }
      )
  }
}
