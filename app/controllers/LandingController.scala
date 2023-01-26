package controllers

import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.DatFileParser
import net.wa9nnn.rc210.data.{Functions, Schedule}
import net.wa9nnn.rc210.model.macros.Macro
import play.api.mvc._

import java.net.URL
import java.nio.file.{Path, Paths}
import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class LandingController @Inject()(val controllerComponents: ControllerComponents, functions: Functions) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val schedulesTable = Table(functions.header, functions.ordered.map(_._2.toRow))
    Ok(views.html.landing(Seq(schedulesTable)))
  }

}