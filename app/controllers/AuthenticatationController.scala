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

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.authentication.{UserManager, UserRecords}
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesInjectedController, MessagesRequest}

import javax.inject.{Inject, Singleton}

/**
 * User Management
 */
@Singleton
class AuthenticatationController @Inject()(implicit config: Config, userManager: UserManager)
  extends MessagesInjectedController with I18nSupport with LazyLogging{
  val userDetailForm: Form[UserEditDTO] = Form {
    mapping(
      "callsign" -> text,
      "name" -> text,
      "email" -> text,
      "id" -> text,
      "password" -> optional(text),
      "password" -> optional(text),
    )(UserEditDTO.apply)(UserEditDTO.unapply)
  }

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global


  def users: Action[AnyContent] = Action { implicit request =>
    val userRecords: UserRecords = userManager.userRecords
    Ok(views.html.users(userRecords))
  }

  def editUser(id: String): Action[AnyContent] = Action { implicit request =>
    userManager.get(id) match {
      case Some(userRecord) =>
        val userDetailData: UserEditDTO = userRecord.userEditDTO
        val userDtoForm: Form[UserEditDTO] = userDetailForm.fill(userDetailData)
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

  def remove(userId: UserId): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    userManager.delete(userId)((Who()))
    Redirect(routes.AuthenticatationController.users())
/*
    request.subject.map { _ =>
      userManager.delete(id)
      Redirect(routes.AuthenticatationController.users())
    }.getOrElse {
      InternalServerError("No subject")
    }
*/
  }

  def saveUser(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
        val binded: Form[UserEditDTO] = userDetailForm.bindFromRequest()
        val maybeString: Option[String] = binded.data.get("submit")
        maybeString.get match {
          case "delete" =>
            binded.data.get("id").foreach((id: UserId) =>
              userManager.delete(id)(Who())
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
                BadRequest(views.html.userEditor(formWithErrors))
              },
              (userDetailData: UserEditDTO) => {
                userManager.put(userDetailData)(Who())
                Redirect(routes.AuthenticatationController.users())
              }
            )
          case x =>
            logger.error(s"unknown command: $x")
            Redirect(routes.AuthenticatationController.users())

        }
  }
}
