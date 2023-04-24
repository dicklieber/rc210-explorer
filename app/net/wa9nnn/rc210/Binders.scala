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

package net.wa9nnn.rc210

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, MessageKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.mvc.PathBindable

object Binders {
  implicit def keyKindPathBinder: PathBindable[KeyKind] = new PathBindable[KeyKind] {
    override def bind(key: String, value: String): Either[String, KeyKind] = {

      Right(KeyKind.valueOf(value))
    }

    override def unbind(key: String, macroKey: KeyKind): String = {
      macroKey.toString
    }
  }

  implicit def pathBinder: PathBindable[MacroKey] = new PathBindable[MacroKey] {
    override def bind(key: String, value: String): Either[String, MacroKey] = {

      Right(KeyFactory(value))
    }

    override def unbind(key: String, macroKey: MacroKey): String = {
      macroKey.toString
    }
  }

  implicit def messageKeyBinder: PathBindable[MessageKey] = new PathBindable[MessageKey] {
    override def bind(key: String, value: String): Either[String, MessageKey] = {

      Right(KeyFactory(value))
    }

    override def unbind(key: String, messageKey: MessageKey): String = {
      messageKey.toString
    }
  }

  implicit def fieldKeyBinder: PathBindable[FieldKey] = new PathBindable[FieldKey] {
    override def bind(key: String, value: String): Either[String, FieldKey] =

      try {
        Right(FieldKey.fromParam(value))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, fieldKey: FieldKey): String =
      fieldKey.param
  }
}
