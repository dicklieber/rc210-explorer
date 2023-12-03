package controllers


import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.security.authentication.RcSession.playSessionName
import net.wa9nnn.rc210.security.authentication.SessionManagerActor.Create
import net.wa9nnn.rc210.security.authentication.*
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, FormError}
import play.api.mvc.*
import play.twirl.api.HtmlFormat

import javax.inject.*
import scala.concurrent.*
import scala.concurrent.duration.*
import scala.language.postfixOps


@Singleton()
class LoginController @Inject()(implicit config: Config,
                                val sessionActor: ActorRef[SessionManagerActor.Message],
                                val userActor: ActorRef[UserManagerActor.Message],
                                scheduler: Scheduler, ec: ExecutionContext,
                                cc: MessagesControllerComponents
                               ) extends MessagesInjectedController with LazyLogging {
setControllerComponents(cc) //todo should not need to do this!

  val loginForm: Form[Credentials] = Form {
    mapping(
      "callsign" -> text,
      "password" -> text,
    )(Credentials.apply)(Credentials.unapply)
  }
  private val ownerMessage: String = config.getString("vizRc210.authentication.message")

  def loginLanding: Action[AnyContent] = Action {
    implicit request =>
      val form = loginForm.fill(Credentials())
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
      val form = loginForm.fill(Credentials())
      Ok(views.html.login(form, ownerMessage, Option.when(errorMessage.nonEmpty) {
        errorMessage
      }))
  }

  private val discardingCookie: DiscardingCookie = DiscardingCookie(playSessionName)
  implicit val timeout: Timeout = 3 seconds

  /**
   * Attempt to authenticate the user
   *
   * @return
   */
  def doLogin(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val binded: Form[Credentials] = loginForm.bindFromRequest()
    binded.fold(
      (formWithErrors: Form[Credentials]) => {
        val errors: Seq[FormError] = formWithErrors.errors
        errors.foreach { err =>
          logger.error(err.message)
        }
        val appendable = views.html.login(formWithErrors, ownerMessage, Option("auth.badlogin"))
        BadRequest(appendable)
      },
      (login: Credentials) => {
        (for {
          user: User <- Await.result[Option[User]](userActor.ask(UserManagerActor.Validate(login, _)), 30 seconds)
          session: RcSession = Await.result[RcSession](sessionActor.ask(Create(user, request.remoteAddress, _)), 3 seconds)
        } yield {
          logger.info(s"Login callsign:${login.callsign}  ip:${request.remoteAddress}")
          Ok(views.html.empty())
            .withSession(RcSession.playSessionName -> session.sessionId)
        })
          .getOrElse {
            logger.error(s"Login Failed callsign:${login.callsign} ip:${request.remoteAddress}")
            Redirect(routes.LoginController.error("Unknown user or bad password!")).discardingCookies(discardingCookie)
          }
      })
  }

  def logout(): Action[AnyContent] = Action.async {
    implicit request =>
      val rcSession: RcSession = request.attrs(sessionKey)
      (sessionActor ? (ref => SessionManagerActor.Remove(rcSession.sessionId, ref))).map { _ =>
        Redirect(routes.LoginController.loginLanding).discardingCookies(discardingCookie)
      }
  }
}
