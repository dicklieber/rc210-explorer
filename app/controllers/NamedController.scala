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
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.named
import net.wa9nnn.rc210.data.named.{NamedKey, NamedManager}
import net.wa9nnn.rc210.key.{Key, KeyFormats, KeyKindEnum, Keys}
import net.wa9nnn.rc210.key.KeyKindEnum.KeyKind
import play.api.data.Form
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import javax.inject.Inject
import play.api.data.validation.Constraints._

import scala.collection.immutable

class NamedController @Inject()(implicit val controllerComponents: ControllerComponents,
                                namedManager: NamedManager) extends BaseController {


  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.named(0, Seq.empty))
  }

  def edit: Action[AnyContent] = Action { implicit request =>

    val kv: Map[String, String] = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }

    namedManager.update(kv.removed("keyKind").map { case (key, value) =>
      val key1 = KeyFormats.parseString(key)
      NamedKey(key1, value)
    })


    val kkIndex: Int = kv("keyKind").toInt
    val selectedKeyKind: KeyKind = KeyKindEnum(kkIndex).asInstanceOf[KeyKind]

    val keys: Seq[Key] = Keys(selectedKeyKind)
    val namedKeys: Seq[NamedKey] = keys
      .map { key =>
        NamedKey(key, namedManager.get(key).getOrElse(""))
      }
    Ok(views.html.named(kkIndex, namedKeys))

  }

}

case class NamedMetadata(selectedKeyKind: Int, namedKeys: Seq[NamedKey])


