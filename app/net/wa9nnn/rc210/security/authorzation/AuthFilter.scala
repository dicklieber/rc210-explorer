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

import javax.inject.Inject
import akka.util.ByteString
import controllers.LoginController
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.authentication.{RcSession, SessionManager, User}
import net.wa9nnn.rc210.security.authentication.SessionManager.playSessionName
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import play.api.Logging
import play.api.libs.streams.Accumulator
import play.api.libs.typedmap.{TypedEntry, TypedKey, TypedMap}
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class AuthFilter @Inject()(implicit loginController: LoginController, sessionManager: SessionManager, ec: ExecutionContext) extends EssentialFilter with Logging {
  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {
      val path = requestHeader.path

      if (path.startsWith("/login") || path.startsWith("/assets"))
        nextFilter(requestHeader)
      else {
        val cookies = requestHeader.cookies
        cookies.find(_.name == playSessionName) match {
          case Some(cookie: Cookie) =>
            sessionManager.lookup(cookie) match {
              case Some(session: RcSession) =>
                logger.info(s"session: $session")
                val te: TypedEntry[RcSession] = TypedEntry(sessionKey, session)
                val requestHeaderWithSession = requestHeader.withAttrs(TypedMap(te))

                val value: Accumulator[ByteString, Result] = nextFilter(requestHeaderWithSession)
                value
              case None =>
                val message = "Bad Callsign or password!"
                logger.error(message)
                val value: Accumulator[ByteString, Result] = loginController.loginLanding(Option("Bad Callsign or password!")).apply(requestHeader)
                value
            }
          case None =>
            logger.error("No session cookie. Redirect to Login ")
            loginController.loginLanding(Option("Bad Callsign or password!")).apply(requestHeader)
        }
      }
    }
  }
}

object AuthFilter {
  val sessionKey: TypedKey[RcSession] = TypedKey[RcSession]("rcSession")

  implicit def h2s(requestHeader: RequestHeader): RcSession = {
    requestHeader.attrs(sessionKey)
  }

  implicit def h2u(requestHeader: RequestHeader): User = {
    requestHeader.attrs(sessionKey).user
  }
  implicit def h2w(requestHeader: RequestHeader): Who = {
    requestHeader.attrs(sessionKey).user.who
  }
}
