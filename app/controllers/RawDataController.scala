package controllers

import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.DataProvider
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.data.functions.Functions
import net.wa9nnn.rc210.data.macros.Macro
import play.api.mvc._

import javax.inject._
import scala.util.Try

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RawDataController @Inject()(val controllerComponents: ControllerComponents, functions: Functions, dataProvider: DataProvider) extends BaseController {


  //  private val datFile: DatFile = datFileSource.datFile()

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def functions(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
        val schedulesTable = Table(functions.header, functions.functions.map(_.toRow))
        Ok(views.html.dat(Seq(schedulesTable)))
  }

  def macros(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val rc210Data: Rc210Data = dataProvider.rc210Data
    val macros = rc210Data.macros
    val macrosTable = Table(Macro.header(macros.length), macros.map(_.toRow))
        Ok(views.html.dat(Seq(macrosTable)))
  }

  def messageMacros(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("//todo")
    //    val macrosTable = Table(MessageMacroNode.header, datFile.messageMacros.messaageMacros.map(_.toRow))
    //    Ok(views.html.dat(Seq(macrosTable)))
  }

  def schedules(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("//todo")
    //    val schedulesTable = Table(ScheduleNode.header, datFile.schedules.map(_.toRow))
    //    Ok(views.html.dat(Seq(schedulesTable)))
  }

}