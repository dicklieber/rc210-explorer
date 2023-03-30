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

import net.wa9nnn.rc210.data.named.{NamedKey, NamedManager}
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.mvc._

import javax.inject.Inject

class NamedController @Inject()(implicit val controllerComponents: ControllerComponents,
                                namedManager: NamedManager) extends BaseController {


  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.named(None, Seq.empty))
  }

  def edit: Action[AnyContent] = Action { implicit request =>
    val kv: Map[String, String] = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }

    namedManager.update(kv.removed("keyKind").map { case (key, value) =>
      val key1: Key = KeyFactory(key)
      NamedKey(key1, value)
    })


    val selectedKeyKind: KeyKind = KeyKind.valueOf(kv("keyKind"))

    val keys: Seq[Key] = KeyFactory(selectedKeyKind)
    val namedKeys: Seq[NamedKey] = keys
      .map { key =>
        NamedKey(key, namedManager.get(key).getOrElse(""))
      }
    Ok(views.html.named(Option(selectedKeyKind), namedKeys))
  }
}

case class NamedMetadata(selectedKeyKind: Int, namedKeys: Seq[NamedKey])


