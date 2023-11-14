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
import net.wa9nnn.rc210.data.clock.Occurrence
import net.wa9nnn.rc210.data.field.MonthOfYearDST
import net.wa9nnn.rc210.key.{LogicAlarmKey, MacroKey, MessageKey, MeterAlarmKey, MeterKey, TimerKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.serial.ComPort
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

  implicit def pathBinderMacro: PathBindable[MacroKey] = new PathBindable[MacroKey] {
    override def bind(key: String, value: String): Either[String, MacroKey] = {

      Right(KeyFactory(value))
    }

    override def unbind(key: String, macroKey: MacroKey): String = {
      macroKey.toString
    }
  }
  implicit def pathBinderTimerKey: PathBindable[TimerKey] = new PathBindable[TimerKey] {
    override def bind(key: String, value: String): Either[String, TimerKey] = {

      Right(KeyFactory(value))
    }

    override def unbind(key: String, timerKey: TimerKey): String = {
      timerKey.toString
    }
  }

  implicit def pathBinderMeter: PathBindable[MeterKey] = new PathBindable[MeterKey] {
    override def bind(key: String, value: String): Either[String, MeterKey] = {

      Right(KeyFactory(value))
    }

    override def unbind(key: String, meterKey: MeterKey): String = {
      meterKey.toString
    }
  }
  implicit def pathBinderMeterAlarm: PathBindable[MeterAlarmKey] = new PathBindable[MeterAlarmKey] {
    override def bind(key: String, value: String): Either[String, MeterAlarmKey] = {

      Right(KeyFactory(value))
    }

    override def unbind(key: String, meterAlarmKey: MeterAlarmKey): String = {
      meterAlarmKey.toString
    }
  }
  implicit def pathBinderLogicAlarmKey: PathBindable[LogicAlarmKey] = new PathBindable[LogicAlarmKey] {
    override def bind(key: String, value: String): Either[String, LogicAlarmKey] = {

      Right(KeyFactory(value))
    }

    override def unbind(key: String, logicAlarmKey: LogicAlarmKey): String = {
      logicAlarmKey.toString
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
        Right(MonthOfYearDST.valueOf(value))
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
        Right(Occurrence.valueOf(value))
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
