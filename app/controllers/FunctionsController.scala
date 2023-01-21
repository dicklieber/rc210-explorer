package controllers

import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.data.Functions
import net.wa9nnn.rc210.data.Function
import play.api.mvc._

import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class FunctionsController @Inject()(val controllerComponents: ControllerComponents, functions:Functions) extends BaseController {

  println(functions)
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    val schedulesTable = Table(Function.header, functions.ordered.map(_._2.toRow))
    Ok(views.html.functions(schedulesTable))
  }
}
