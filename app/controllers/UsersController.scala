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

import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.util.Timeout
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc.{Action, AnyContent, MessagesInjectedController, MessagesRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.{implicitConversions, postfixOps}

/**
 * User Management
 */

/**
 * Handle create/edit/list users/
 */
@Singleton
class UsersController @Inject()(val userActor: ActorRef[UserManagerActor.Message])(
  implicit scheduler: Scheduler
)
  extends MessagesInjectedController with LazyLogging {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val timeout: Timeout = 3 seconds

  def users: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    userActor.ask(UserManagerActor.Users.apply)
      .map(userRecords =>
        Ok(views.html.users(userRecords))
      )
  }

  def editUser(id: UserId): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    userActor.ask(Get(id, _))
      .map {
        case Some(user: User) =>
          Ok(views.html.userEditor(user.userEditDTO))
        case None =>
          Ok(s"UserId: $id not found!")
      }
  }

  def addUser(): Action[AnyContent] = Action { implicit request =>
    val newUser: UserEditDTO = UserEditDTO()
    Ok(views.html.userEditor(newUser))
  }

  def remove(userId: UserId): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    userActor.ask(Remove(userId, user, _)).map { _ =>
      Redirect(routes.UsersController.users())
    }
  }

  def saveUser(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>

    val helper = FormHelper()

    val userEditDTO = UserEditDTO(helper)

    userActor.ask(Put(userEditDTO, helper.user, _)).map { _ =>
      Redirect(routes.UsersController.users())
    }
  }
}
