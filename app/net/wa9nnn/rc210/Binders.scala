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

import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.clock.{MonthOfYearDST, Occurrence}
import net.wa9nnn.rc210.data.field.FieldKey
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.serial.ComPort
import play.api.mvc.PathBindable

object Binders {
  implicit def keyKindPathBinder: PathBindable[KeyKind] = new PathBindable[KeyKind] {
    override def bind(key: String, value: String): Either[String, KeyKind] = Right(KeyKind.valueOf(value))

    override def unbind(key: String, macroKey: KeyKind): String = macroKey.toString
  }

  implicit def pathBinderMacro: PathBindable[Key] = new PathBindable[Key] {
    override def bind(key: String, value: String): Either[String, Key] = {

      Right(Key((value)))
    }

    override def unbind(key: String, macroKey: Key): String = {
      macroKey.toString
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


  implicit def monthOfYearDSTBinder: PathBindable[MonthOfYearDST] = new PathBindable[MonthOfYearDST] {
    override def bind(key: String, value: String): Either[String, MonthOfYearDST] =
      try {
        Right(MonthOfYearDST.lookup(value))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, monthOfYearDST: MonthOfYearDST): String =
      monthOfYearDST.toString
  }

  implicit def occurrenceBinder: PathBindable[Occurrence] = new PathBindable[Occurrence] {
    override def bind(key: String, value: String): Either[String, Occurrence] =
      try {
        Right(Occurrence.lookup(value))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, occurrence: Occurrence): String =
      occurrence.toString
  }

  implicit def comportBinder: PathBindable[ComPort] = new PathBindable[ComPort] {
    override def bind(key: String, value: String): Either[String, ComPort] =
      try {
        Right(ComPort(value))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, comPort: ComPort): String =
      comPort.toString()
  }
}
