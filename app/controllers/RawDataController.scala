package controllers

import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.DatFileIo
import net.wa9nnn.rc210.data.Functions
import net.wa9nnn.rc210.model.Schedule
import net.wa9nnn.rc210.model.macros.Macro
import play.api.mvc._

import java.nio.file.Paths
import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RawDataController @Inject()(val controllerComponents: ControllerComponents, functions: Functions) extends BaseController {
  private val url1 = getClass.getResource("/examples/schedExamples.dat")
  private val path = Paths.get(url1.toURI)
  private val datFile = DatFileIo(path)

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def functions(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val schedulesTable = Table(functions.header, functions.ordered.map(_._2.toRow))
    Ok(views.html.dat(Seq(schedulesTable)))
  }

  def macros(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val macrosTable = Table(Macro.header, Macro.extractMacros(datFile).map(_.toRow))
    Ok(views.html.dat(Seq(macrosTable)))
  }

  def schedules(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val schedulesTable = Table(Schedule.header, Schedule.extractSchedules(datFile).map(_.toRow))
    Ok(views.html.dat(Seq(schedulesTable)))
  }

}