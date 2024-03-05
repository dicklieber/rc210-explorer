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

import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.ui.nav.TabKind
import play.api.mvc.PathBindable

object Binders {

  implicit def pathBinderMacro: PathBindable[Key] = new PathBindable[Key] {
    override def bind(key: String, value: String): Either[String, Key] = {

      Right(Key((value)))
    }

    override def unbind(key: String, macroKey: Key): String = {
      macroKey.toString
    }
  }

/*  implicit def fieldKeyPathBinder(implicit intBinder: PathBindable[FieldKey]): PathBindable[FieldKey] = new PathBindable[FieldKey] {
    override def bind(key: String, fromPath: String): Either[String, FieldKey] = {
      try {
        Right(FieldKey(fromPath))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }
    }

    override def unbind(key: String, fieldKeyStuff: FieldKey): String =
      fieldKeyStuff.toString
  }*/


  implicit def fieldKeyBinder: PathBindable[FieldKey] = new PathBindable[FieldKey] {
    override def bind(key: String, id: String): Either[String, FieldKey] =

      try {
        Right(FieldKey.fromId(id))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, fieldKey: FieldKey): String =
      fieldKey.id
  }
//  implicit def tabKeyBinder: PathBindable[TabKind] = new PathBindable[TabKind] {
//    override def bind(key: String, value: String): Either[String, TabKind] =
//
//      try {
//        Right(FieldKey(value))
//      } catch {
//        case e: Exception =>
//          Left(e.getMessage)
//      }
//
//    override def unbind(key: String, tabKind: TabKind): String =
//      tabKind.toString
//  }

  implicit def userIdBinder: PathBindable[UserId] = new PathBindable[UserId] {
    override def bind(key: String, value: String): Either[String, UserId] =

      try {
        Right(value)
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, userId: UserId): String =
      userId
  }
  
}
