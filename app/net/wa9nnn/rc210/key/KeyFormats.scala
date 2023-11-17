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

package net.wa9nnn.rc210.key

import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.data.functions.FunctionNode
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.named.NamedKey
import play.api.libs.json.*
import play.api.mvc.PathBindable

import scala.language.postfixOps
import scala.util.Try
import scala.util.matching.Regex

object KeyFormats {


  val r: Regex = """([a-zA-Z]+)(\d+)?""".r

  implicit def keyKindPathBinder(implicit intBinder: PathBindable[KeyKind]): PathBindable[KeyKind] = new PathBindable[KeyKind] {
    override def bind(key: String, fromPath: String): Either[String, KeyKind] = {
      try {
        Right(KeyKind.valueOf(fromPath))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }
    }

    override def unbind(key: String, keyKind: KeyKind): String =
      keyKind.toString
  }


  implicit val fmtNamed: OFormat[NamedKey] = Json.format[NamedKey]



}
