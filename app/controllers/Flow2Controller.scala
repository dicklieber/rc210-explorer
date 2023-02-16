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

import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.{DataProvider, MacroKey}
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import play.api.mvc._

import javax.inject.Inject

class Flow2Controller @Inject()(implicit val controllerComponents: ControllerComponents,
                                dataProvider: DataProvider,
                                functionsProvider: FunctionsProvider) extends BaseController {

  def flow(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>


    implicit val rc210Data = dataProvider.rc210Data
    val head = rc210Data.macros.head
    val headTable: Table = head.table()
//      .withCssClass("table")

    Ok(views.html.flow(Seq(headTable)))
  }

}
case class MacroBlock(maceroKey:MacroKey, table: Table)