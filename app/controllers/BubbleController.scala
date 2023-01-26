package controllers

import net.wa9nnn.rc210.data.{D3DataBuilder, DatFileSource, Functions}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.language.postfixOps

@Singleton
class BubbleController @Inject()(val controllerComponents: ControllerComponents, d3DataBuilder: D3DataBuilder, datFileSource: DatFileSource) extends BaseController {

  def d3: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.bubbleSvg())
  }

  def svg: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val datFile = datFileSource.datFile()
    val d3Data = d3DataBuilder.aoply(datFile)

    val sjson = Json.prettyPrint(Json.toJson(d3Data))
    Ok(sjson)
  }
}