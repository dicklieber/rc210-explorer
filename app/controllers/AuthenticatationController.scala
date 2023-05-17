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

import be.objectify.deadbolt.scala.{ActionBuilders, AuthenticatedRequest}
import com.typesafe.config.Config
import net.wa9nnn.rc210.security.RcRole
import net.wa9nnn.rc210.security.RcRole.{adminRole, tempAdminRole}
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.authentication.{UserManager, UserRecords}
import play.api.data.Forms.{mapping, of, optional, text}
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, ControllerComponents, MessagesInjectedController}
import play.mvc.Controller

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

/**
 * User Management
 */
@Singleton
class AuthenticatationController @Inject()(implicit
                                           config: Config,
                                           userManager: UserManager,
                                           actionBuilder: ActionBuilders,
                                           cc: ControllerComponents) extends Controller {
  val userDetailForm: Form[UserEditDTO] = Form {
    mapping(
      "callsign" -> text,
      "name" -> text,
      "email" -> text,
      "role" -> of[RcRole],
      "id" -> text,
      "password" -> optional(text),
      "password" -> optional(text),
    )(UserEditDTO.apply)(UserEditDTO.unapply)
  }

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global


  def users: Action[AnyContent] = be.objectify.deadbolt.scala.SubjectActionBuilder(adminRole, tempAdminRole) {
    implicit request =>
      Future {
        val userRecords: UserRecords = userManager.userRecords
        Ok(views.html.users(userRecords))
      }
  }

  def editUser(id: String): Action[AnyContent] = allowRoles(adminRole, tempAdminRole) {
    implicit request =>
      Future {
        userManager.get(id) match {
          case Some(userRecord) =>
            val userDetailData: UserEditDTO = userRecord.userEditDTO
            val value: Form[UserEditDTO] = userDetailForm.fill(userDetailData)
            Ok(views.html.userEditor(value))
          case None =>
            Ok(s"UUID: $id not found!")
        }
      }
  }

  def addUser(): Action[AnyContent] = allowRoles(adminRole) {
    implicit request =>
      val userDetailData = UserEditDTO()
      val emptyForm = userDetailForm.fill(userDetailData)
      Future(
        Ok(views.html.security.userEditor(emptyForm))
      )
  }

  def remove(id: String): Action[AnyContent] = allowRoles(adminRole) {
    implicit request: AuthenticatedRequest[AnyContent] =>
      Future {
        request.subject.map { _ =>
          userManager.delete(id)
          Redirect(routes.AuthenticatationController.users())
        }.getOrElse {
          InternalServerError("No subject")
        }
      }
  }

  def saveUser(): Action[AnyContent] = allowRoles(adminRole) {
    implicit request: AuthenticatedRequest[AnyContent] =>
      Future {
        val binded: Form[UserEditDTO] = userDetailForm.bindFromRequest()
        val maybeString: Option[String] = binded.data.get("submit")
        maybeString.get match {
          case "delete" =>
            binded.data.get("id").foreach((id: UserId) =>
              userManager.delete(id)
            )
            Redirect(routes.AuthenticatationController.users())
          case "cancel" =>
            Redirect(routes.AuthenticatationController.users())
          case "save" =>
            binded.fold(
              (formWithErrors: Form[UserEditDTO]) => {
                val errors: Seq[FormError] = formWithErrors.errors
                errors.foreach { err =>
                  logger.error(err.message)
                }
                BadRequest(views.html.security.userEditor(formWithErrors))
              },
              (userDetailData: UserEditDTO) => {
                userManager.put(userDetailData)
                Redirect(routes.AuthenticatationController.users())
              }
            )
          case x =>
            logger.error(s"unknown command: $x")
            Redirect(routes.AuthenticatationController.users())

        }
      }
  }
}
