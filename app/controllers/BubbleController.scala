package controllers

import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.data.{DatFileSource, FlowTableBuilder, Functions}
import net.wa9nnn.rc210.model.DatFile
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.language.postfixOps

@Singleton
class BubbleController @Inject()(val controllerComponents: ControllerComponents, d3DataBuilder: FlowTableBuilder, datFileSource: DatFileSource) extends BaseController {

  def bubble: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val datFile: DatFile = datFileSource.datFile()
    val value: Table = d3DataBuilder.aoply(datFile)
    Ok(views.html.dat(Seq(value)))
  }

}
