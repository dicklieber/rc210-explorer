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
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.KeyFormats
import play.api.mvc._

import javax.inject.Inject

class FieldEditorController @Inject()(implicit val controllerComponents: ControllerComponents,
                                      dataProvider: DataProvider,
                                      namedManager: NamedManager,
                                      functionsProvider: FunctionsProvider) extends BaseController {


  def selectKey(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>



      implicit val rc210Data = dataProvider.rc210Data
      val mappedValues = rc210Data.mappedValues
      val knownKeys = mappedValues.knownKeys

      Ok(views.html.selectKey(knownKeys))
  }
  def editFields(sKey: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>


      val key = KeyFormats.parseString(sKey)

      implicit val rc210Data = dataProvider.rc210Data
      val mappedValues = rc210Data.mappedValues

      Ok(views.html.fieldsEditor(mappedValues.fieldsForKey(key)))
  }


  def save(): Action[AnyContent] = Action { request: Request[AnyContent] =>
    val body: AnyContent = request.body
    val formUrlEncoded = body.asFormUrlEncoded
//    val value: Option[Map[String, Seq[String]]] = formUrlEncoded
//        val jsonBody: Option[JsValue] = body.asJson

    Ok("todo")
  }
}