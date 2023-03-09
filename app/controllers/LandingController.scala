package controllers

import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import play.api.mvc._

import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class LandingController @Inject()(val controllerComponents: ControllerComponents, functions: FunctionsProvider) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.landing())
  }


  def tabs: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.tabs())
  }
}