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
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.security.authentication.SessionManager.playSessionName
import net.wa9nnn.rc210.security.authentication.{Login, RcSession, SessionManagerActor, UserManager}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, FormError}
import play.api.mvc._
import play.twirl.api.HtmlFormat

import javax.inject._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps


@Singleton
class LoginController @Inject()(implicit config: Config,
                                userManager: UserManager,
                                val actor: ActorRef[SessionManagerActor.SessionManagerMessage],
                                scheduler: Scheduler, ec: ExecutionContext
                               ) extends MessagesInjectedController with LazyLogging {
  //  extends MessagesAbstractController

  val loginForm: Form[Login] = Form {
    mapping(
      "callsign" -> text,
      "password" -> text,
    )(Login.apply)(Login.unapply)
  }
  private val ownerMessage: String = config.getString("vizRc210.authentication.message")
  implicit val timeout: Timeout = 3 seconds

  def loginLanding: Action[AnyContent] = Action {
    implicit request =>
      val form = loginForm.fill(Login())
      try {
        val appendable: HtmlFormat.Appendable = views.html.login(form, ownerMessage)
        Ok(appendable)
      } catch {
        case e: Exception =>
          val message = e.getMessage
          logger.error(message, e)
          Ok(views.html.login(form, ownerMessage))
      }
  }

  def error(errorMessage: String): Action[AnyContent] = Action {
    implicit request =>
      val form = loginForm.fill(Login())
      Ok(views.html.login(form, ownerMessage, Option.when(errorMessage.nonEmpty) {
        errorMessage
      }))
  }

  private val discardingCookie: DiscardingCookie = DiscardingCookie(playSessionName)

  /**
   * Attempt to authenticate the user
   *
   * @return
   */
  def dologin(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val binded: Form[Login] = loginForm.bindFromRequest()
    binded.fold(
      (formWithErrors: Form[Login]) => {
        val errors: Seq[FormError] = formWithErrors.errors
        errors.foreach { err =>
          logger.error(err.message)
        }
        //          val destination = formWithErrors.data.get("destination")
        val appendable = views.html.login(formWithErrors, ownerMessage, Option("auth.badlogin"))
        Future(BadRequest(appendable))
      },
      (login: Login) => {
        userManager.validate(login)
          .map { user =>
            val future: Future[RcSession] = actor.ask(ref => SessionManagerActor.Create(user, request.remoteAddress, ref))
            future.map { rcSession =>
              logger.info(s"Login callsign:${login.callsign}  ip:${request.remoteAddress}")
              Ok(views.html.empty())
                .withSession(RcSession.playSessionName -> rcSession.sessionId)
            }
          }
          .getOrElse {
            logger.error(s"Login Failed callsign:${login.callsign} ip:${request.remoteAddress}")
            Future(Redirect(routes.LoginController.error("Unknown user or bad password!")).discardingCookies(discardingCookie))
          }
      })
  }

  def logout(): Action[AnyContent] = Action.async {
    implicit request =>
      val rcSession: RcSession = request.attrs(sessionKey)
      (actor ? (ref => SessionManagerActor.Remove(rcSession.sessionId, ref))).map { _ =>
        Redirect(routes.LoginController.loginLanding).discardingCookies(discardingCookie)
      }
  }
}
