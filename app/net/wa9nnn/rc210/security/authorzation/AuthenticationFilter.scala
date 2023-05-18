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

package net.wa9nnn.rc210.security.authorzation

import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import controllers.LoginController
import net.wa9nnn.rc210.security.authentication.{RcSession, SessionManager}
import net.wa9nnn.rc210.security.authentication.SessionManager.playSessionName
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticationFilter @Inject()(loginController: LoginController, sessionManager: SessionManager)(implicit val mat: Materializer, ec: ExecutionContext) extends Filter with LazyLogging {
  logger.info("AuthenticationFilter")

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    val cookies = requestHeader.cookies
    cookies.find(_.name == playSessionName) match {
      case Some(cookie: Cookie) =>
        sessionManager.lookup(cookie) match {
          case Some(session: RcSession) => ???
          case None =>
            val message = "Bad Callsign or password!"
            logger.error(message)
            loginController.loginLanding(Option("Bad Callsign or password!"))
        }
      case None =>
        logger.error("No session cookie. Redirect to Login ")
        loginController.loginLanding()
    }

    nextFilter(requestHeader).map { result =>

      result
    }
  }
}