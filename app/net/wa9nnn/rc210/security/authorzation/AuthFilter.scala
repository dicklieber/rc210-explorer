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
import controllers.routes
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.authentication.RcSession.playSessionName
import net.wa9nnn.rc210.security.authentication.{RcSession, SessionManager, User}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import play.api.libs.typedmap.{TypedEntry, TypedKey, TypedMap}
import play.api.mvc.Results.Redirect
import play.api.mvc._
import play.api.mvc.request.{Cell, RequestAttrKey}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class AuthFilter @Inject()(implicit val mat: Materializer, sessionManager: SessionManager, ec: ExecutionContext) extends Filter with LazyLogging {

  override def apply(next: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val path: String = requestHeader.path
    if (path.startsWith("/login") || path.startsWith("/assets") || path == "/") {
      logger.trace("Don't look for session in path: {}", path)
      next(requestHeader)
    } else {
      logger.trace("Looking for session by cookie with SessionId")


      val playSession: Session = requestHeader.session


      val s: Option[RequestHeader] = for {
        sessionId <-  playSession.get(playSessionName)
        session <- sessionManager.lookup(sessionId)
      } yield {
        logger.trace("Got valid session from cookie")
        val te: TypedEntry[RcSession] = TypedEntry(sessionKey, session)
        val requestHeaderWithSession: RequestHeader = requestHeader.withAttrs(TypedMap(te))
        requestHeaderWithSession
      }
      val r: Future[Result] = s match {
        case Some(requestHeaderWithSessiobn) =>
          logger.trace("Invoking next")
          val r: Future[Result] = next(requestHeaderWithSessiobn)
          r
        case None =>
          Future {
            logger.trace("Redirect to loginLanding page.")
            val result: Result = Redirect(routes.LoginController.loginLanding)
            result
          }
      }
      r
    }
  }
}

object AuthFilter extends LazyLogging {
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

//class AuthenticatedUserAction @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext)
//  extends ActionBuilderImpl(parser) {
//
//  private val logger = play.api.Logger(this.getClass)
//
//  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
//    logger.debug("ENTERED AuthenticatedUserAction::invokeBlock")
//    val user:User = request
//    val ar: AuthenticatedRequest[A, User] = new AuthenticatedRequest(user, request)
//
//
//    Security.Authenticated(ar)
//
//
//    val builder: AuthenticatedBuilder[User] = AuthenticatedBuilder[User]
//    builder.authenticate()
//    authenticatedRequest.authenticate(request )
//    val res: Future[Result] = block(authenticatedRequest)
//    res
//
//
//    maybeUsername match {
//      case None => {
//        logger.debug("CAME INTO 'NONE'")
//        Future.successful(Forbidden("Dude, you’re not logged in."))
//      }
//      case Some(u: String) => {
//        logger.debug("CAME INTO 'SOME'")
//
//        val authenticatedRequest: AuthenticatedBuilder[User] = AuthenticatedBuilder[User]
//        val res: Future[Result] = block(authenticatedRequest)
//        res
//      }
//    }
//  }
//}