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

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import controllers.routes
import net.wa9nnn.rc210.security.authentication
import net.wa9nnn.rc210.security.authentication.RcSession.playSessionName
import net.wa9nnn.rc210.security.authentication.SessionManagerActor.Lookup
import net.wa9nnn.rc210.security.authentication.{RcSession, SessionManagerActor, User}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import play.api.libs.typedmap.{TypedEntry, TypedKey, TypedMap}
import play.api.mvc.Results.Redirect
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}

/**
 * A Play filter that puts the current [[RcSession]] in to the Request or redirects to the Login Page.
 */
class AuthFilter @Inject()(implicit val mat: Materializer,
                           actor: ActorRef[SessionManagerActor.SessionManagerMessage],
                           scheduler: Scheduler,
                           ec: ExecutionContext) extends Filter with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  override def apply(next: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val path: String = requestHeader.path
    if (path.startsWith("/login") || path.startsWith("/assets") || path == "/") {
      logger.trace("Don't look for session in path: {}", path)
      next(requestHeader)
    } else {
      logger.trace("Looking for session by cookie with SessionId")

      val eventualMaybeSession: Future[Option[RcSession]] = actor.ask(ref => Lookup("123", ref)).mapTo[Option[RcSession]]
      val playSession = requestHeader.session
      (for {
        sessionId <- playSession.get(playSessionName)
        session <-  Await.result[Option[authentication.RcSession]](actor.ask(ref => Lookup(sessionId, ref)), 3 seconds)
      } yield {
        logger.trace("Got valid session from cookie")
        val requestHeaderWithSession: RequestHeader = requestHeader.addAttr(sessionKey, session)
        requestHeaderWithSession
      }) match {
        case Some(requestHeaderWithSessiobn: Any) =>
          logger.trace("Invoking next")
          next(requestHeaderWithSessiobn)
        case None =>
          Future {
            logger.trace("Redirect to loginLanding page.")
            Redirect(routes.LoginController.loginLanding)
          }
      }
    }
  }
}

object AuthFilter extends LazyLogging {
  val sessionKey: TypedKey[RcSession] = TypedKey[RcSession]("rcSession")

  //  implicit def h2s(requestHeader: Request[AnyContent]): RcSession = {
  //    requestHeader.attrs(sessionKey)
  //  }

  implicit def who(implicit request: Request[_]): User = {
    request.attrs(sessionKey).user
  }

  //  implicit def h2w(requestHeader: Request[AnyContent]): Who = {
  //    requestHeader.attrs(sessionKey).user.who
  //  }
}
