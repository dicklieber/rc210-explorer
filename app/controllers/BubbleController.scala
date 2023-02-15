/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers

import net.wa9nnn.rc210.DataProvider
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.model.TriggerDetail
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.language.postfixOps
import net.wa9nnn.rc210.data.Formats._

@Singleton
class BubbleController @Inject()(val controllerComponents: ControllerComponents, dataProvider: DataProvider) extends BaseController {

  def bubble(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.bubbleSvg())
  }

  def bubbleData: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val bubbleFlowData = BubbleFlowData(dataProvider.rc210Data)
    val sJson = Json.prettyPrint(Json.toJson(bubbleFlowData))
    Ok(sJson).withHeaders(
      "Content-Type" -> "text/json"
    )
  }
}

/**
 * @param triggerds
 * @param macroNodes
 */
case class BubbleFlowData(triggers: Seq[TriggerDetail], macroNodes: Seq[MacroNode])

object BubbleFlowData {
  import net.wa9nnn.rc210.model.TriggerNode._

  def apply(rc210Data: Rc210Data): BubbleFlowData = {
    val triggers: Seq[TriggerDetail] = rc210Data.enabledTriggers.map(_.triggerDetail)
    new BubbleFlowData(triggers, rc210Data.macros)
  }
}
