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
import net.wa9nnn.rc210.DataProvider
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.named.NamedManager
import play.api.mvc._

import javax.inject.Inject

class Flow2Controller @Inject()(implicit val controllerComponents: ControllerComponents,
                                namedManager: NamedManager,
                                functionsProvider: FunctionsProvider) extends BaseController {

  def flow(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>


//todo get from store
//      val macroNodes: Seq[MacroNode] = rc210Data
//        .macros
//        .filter(_.nodeEnabled)
//
//      Ok(views.html.flow(macroNodes))
      Ok(views.html.flow(Seq.empty))
  }
}