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

package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.key.{FunctionKey, Key, KeyFormats, MacroKey}
import play.api.data.FormError

/**
 * URL formatters.
 * Converts HTML form values to and from application objects.
 */
object Formatters {

  import play.api.data.format.Formatter
  import play.api.data.format.Formats._

  implicit object MacroKeyFormatter extends Formatter[MacroKey] {
    override val format: Option[(String, Nil.type)] = Some(("format.macrokey", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], MacroKey] = parsing(KeyFormats[MacroKey], "error.url", Nil)(key, data)

    override def unbind(key: String, value: MacroKey): Map[String, String] = Map(key -> value.toString)
  }


  implicit object FunctionKeyFormatter extends Formatter[FunctionKey] {
    override val format: Option[(String, Nil.type)] = Some(("format.functionKey", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], FunctionKey] = parsing(KeyFormats[FunctionKey], "error.url", Nil)(key, data)

    override def unbind(key: String, value: FunctionKey): Map[String, String] = Map(key -> value.toString)
  }

  implicit object DtmfFormatter extends Formatter[Dtmf] {
    override val format: Option[(String, Nil.type)] = Some(("format.dtmf", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Dtmf] = parsing(Dtmf(_), "error.url", Nil)(key, data)

    override def unbind(key: String, value: Dtmf): Map[String, String] = Map(key -> value.toString)
  }


}