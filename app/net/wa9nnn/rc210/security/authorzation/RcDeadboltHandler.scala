package net.wa9nnn.rc210.security.authorzation

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import com.typesafe.scalalogging.LazyLogging
import controllers.routes
import net.wa9nnn.rc210.security.authentication.{SessionManager, UserManager}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import be.objectify.deadbolt.scala

@Singleton
class RcDeadboltHandler @Inject()(userManager: UserManager, sessionManager: SessionManager) extends DeadboltHandler with LazyLogging {

  override def getSubject[A](request: scala.AuthenticatedRequest[A]): Future[Option[Subject]] =
    Future {
      for {
        cookie <- request.cookies.get(SessionManager.playSessionName)
        session <- sessionManager.lookup(cookie)
      } yield {
        session
      }
    }


  override def onAuthFailure[A](request: scala.AuthenticatedRequest[A]): Future[Result] =
    Future {
      logger.debug(s"Not authorized redirect to login from ${request.remoteAddress}")
      Redirect(routes.LoginController.loginLanding(Option("auth.badlogin")))
    }

  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future(None)

}