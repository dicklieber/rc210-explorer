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
import net.wa9nnn.rc210.security.authentication.SessionManager.playSessionName
import net.wa9nnn.rc210.security.authentication.{Login, RcSession, SessionManager, UserManager}
import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, FormError}
import play.api.i18n._
import play.api.mvc._

import javax.inject._


@Singleton
class LoginController @Inject()(implicit config: Config,
                                userManager: UserManager,
                                sessionManager: SessionManager,
                               ) extends MessagesInjectedController with LazyLogging with I18nSupport {
  //  extends MessagesAbstractController

  val loginForm: Form[Login] = Form {
    mapping(
      "callsign" -> text,
      "password" -> text,
    )(Login.apply)(Login.unapply)
  }
  val ownerMessage: String = "todo have ui for this"
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global


  //  def index = Action {
  //    val messages: Messages = request.messages
  //    val message: String = messages("info.error")
  //    Ok(message)
  //  }

  /**
   *
   * @param maybeMessage show as error message.
   */
  def loginLanding(maybeMessage: Option[String] = None) = Action {
    implicit request: RequestHeader =>
      val form = loginForm.fill(Login())
      Ok(views.html.login(form, ownerMessage, errorMessage = maybeMessage))
  }

  private val discardingCookie: DiscardingCookie = DiscardingCookie(playSessionName)

  /**
   * Attempt to authenticate the user
   *
   * @return
   */
  def dologin(): Action[AnyContent] = Action{ implicit request: Request[AnyContent] =>
      val binded: Form[Login] = loginForm.bindFromRequest()
      binded.fold(
        (formWithErrors: Form[Login]) => {
          val errors: Seq[FormError] = formWithErrors.errors
          errors.foreach { err =>
            logger.error(err.message)
          }
          //          val destination = formWithErrors.data.get("destination")
          BadRequest(views.html.login(formWithErrors, ownerMessage, Option("auth.badlogin")))
        },
        (login: Login) => {
          userManager.validate(login)
            .map { user =>
              val session = sessionManager.create(user)
              session
            } match {
            case Some(rcSession: RcSession) =>
              logger.info(s"Login callsign:${login.callsign}")
              Redirect(routes.PortsEditorController.index()).withCookies(rcSession.cookie)
            case None =>
              logger.error(s"Login Failed callsign:${login.callsign} ip:${request.remoteAddress}")
              Redirect(routes.LoginController.loginLanding(None)).discardingCookies(discardingCookie)
          }
        })

  }

  def logout(): Action[AnyContent] = Action {
    implicit request =>
      //todo val maybeSession: Option[RcSession] = r2Session(request)
      val maybeSession: Option[RcSession] = None
      maybeSession.foreach { session: RcSession =>
        sessionManager.remove(session.sessionId)
        logger.info(s"Logout callsign:${session.callsign} ip:${request.remoteAddress}")
      }

      Redirect(routes.LoginController.loginLanding(None)).discardingCookies(discardingCookie)
  }
}
