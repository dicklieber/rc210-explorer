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

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.authentication.UserManagerActor
import net.wa9nnn.rc210.security.authentication.UserManagerActor.{Get, Put, Remove}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
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
  private val userDetailForm: Form[UserEditDTO] = Form {
    mapping(
      "callsign" -> text,
      "name" -> optional(text),
      "email" -> optional(text),
      "id" -> text,
      "password" -> optional(text),
      "password" -> optional(text),
    )(UserEditDTO.apply)(UserEditDTO.unapply)
  }

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val timeout: Timeout = 3 seconds

  def users: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    userActor.ask(UserManagerActor.Users)
      .map(userRecords =>
        Ok(views.html.users(userRecords))
      )
  }

  def editUser(id: UserId): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    userActor.ask(Get(id, _))
      .map {
        case Some(user) =>
          val userDtoForm: Form[UserEditDTO] = userDetailForm.fill(user.userEditDTO)
          Ok(views.html.userEditor(userDtoForm))
        case None =>
          Ok(s"UserId: $id not found!")
      }
  }

  def addUser(): Action[AnyContent] = Action { implicit request =>
    val userDetailData = UserEditDTO()
    val emptyForm = userDetailForm.fill(userDetailData)
    Ok(views.html.userEditor(emptyForm))
  }

  def remove(userId: UserId): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    userActor.ask(Remove(userId, who, _)).map { _ =>
      Redirect(routes.UsersController.users())
    }
  }

  def saveUser(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    userDetailForm.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.userEditor(formWithErrors)))
      },
      (userEditDTO: UserEditDTO) => {
        userActor.ask(Put(userEditDTO, who, _)).map { _ =>
          Redirect(routes.UsersController.users())
        }
      }
    )
  }
}
