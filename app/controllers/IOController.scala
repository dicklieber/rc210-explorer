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
import play.api.libs.json.Json
import play.api.mvc._
import net.wa9nnn.rc210.key.KeyFormats._

import javax.inject.{Inject, Singleton}

@Singleton
class IOController @Inject()(val controllerComponents: ControllerComponents, dataProvider: DataProvider) extends BaseController {
    def downloadJson(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
      val sJson = Json.prettyPrint(Json.toJson(dataProvider.rc210Data))

      Ok(sJson).withHeaders(
        "Content-Type" -> "text/json",
        "Content-Disposition" -> s"""attachment; filename="rc210.json""""
      )
    }
}
