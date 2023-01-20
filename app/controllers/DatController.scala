package controllers

import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.{DatFileIo, Schedule}
import play.api.mvc._

import java.net.URL
import java.nio.file.{Path, Paths}
import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class DatController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>

    val url1: URL = getClass.getResource("/examples/schedExamples.dat")
    val path: Path = Paths.get(url1.toURI)
    val datFile = DatFileIo(path)


    val schedules: Seq[Schedule] = Schedule.extractSchedules(datFile)


    val table = Table(Schedule.header, schedules.map(_.toRow))


    Ok(views.html.dat(table))
  }
}
