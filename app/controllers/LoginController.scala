package controllers

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.security.authentication.RcSession.playSessionName
import net.wa9nnn.rc210.security.authentication.*
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.nav.TabKind
import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, FormError}
import play.api.mvc.*
import play.twirl.api.HtmlFormat

import javax.inject.*

@Singleton()
class LoginController @Inject()(implicit config: Config,
                                sessionManager: SessionStore,
                                userManager: UserStore,
                                cc: MessagesControllerComponents
                               ) extends MessagesInjectedController with LazyLogging {
  setControllerComponents(cc) //todo should not need to do this!

  private val loginForm: Form[Credentials] = Form {
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
      (credentials: Credentials) => {

        (for {
          user: User <- userManager.validate(credentials)
          session: RcSession = sessionManager.create(user, request.remoteAddress)
        } yield {
          logger.info(s"Login callsign:${credentials.callsign}  ip:${request.remoteAddress}")
          Ok(views.html.landing(TabKind.Fields))
            .withSession(RcSession.playSessionName -> session.sessionId)
        })
          .getOrElse {
            logger.error(s"Login Failed callsign:${credentials.callsign} ip:${request.remoteAddress}")
            Redirect(routes.LoginController.error("Unknown user or bad password!")).discardingCookies(discardingCookie)
          }
      })
  }

  def logout(): Action[AnyContent] = Action {
    implicit request =>
      val rcSession: RcSession = request.attrs(sessionKey)
      sessionManager.remove(rcSession.sessionId)
      Redirect(routes.LoginController.loginLanding).discardingCookies(discardingCookie)
  }
}
