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
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.FieldEditor
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.KeyFormats
import play.api.mvc._

import javax.inject.Inject
import scala.collection.immutable

class FieldEditorController @Inject()(implicit val controllerComponents: ControllerComponents,
                                      dataProvider: DataProvider,
                                      fieldEditor: FieldEditor,
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
  def editCards(sKey: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>


      val key = KeyFormats.parseString(sKey)

      implicit val rc210Data = dataProvider.rc210Data
      val mappedValues = rc210Data.mappedValues

      Ok(views.html.cards(mappedValues.fieldsForKey(key)))
  }

  def editOne(sKey: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>


      implicit val rc210Data = dataProvider.rc210Data

      val fieldKey: FieldKey = FieldKey.fromParam(sKey)
      rc210Data.mappedValues.entity(fieldKey) match {
        case Some(fieldEntry) =>
          Ok(views.html.fieldsEditor(Seq(fieldEntry)))
        case None =>
          NotFound {
            s"No key: $sKey"
          }
      }

  }


  def save(): Action[AnyContent] = Action { request: Request[AnyContent] =>
    val body: AnyContent = request.body
    val formUrlEncoded: Option[Map[String, Seq[String]]] = body.asFormUrlEncoded
    val lines: immutable.Iterable[String] = for {
      case (name, values) <- formUrlEncoded.get
    } yield {
      s"""$name, "${values.mkString(",")}"\n"""
    }

    Ok(lines.mkString("\n"))
  }
}