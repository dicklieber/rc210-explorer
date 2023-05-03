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
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.key.KeyFactory._
import play.api.data.FormError
import play.api.libs.json.{Json, OFormat}

/**
 * URL formatters.
 * Converts HTML form values to and from application objects.
 */
object Formatters {

  import play.api.data.format.Formats._
  import play.api.data.format.Formatter

  implicit object MacroKeyFormatter extends Formatter[MacroKey] {
    override val format: Option[(String, Nil.type)] = Some(("format.macrokey", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], MacroKey] = parsing(s => KeyFactory(s), "error.url", Nil)(key, data)

    override def unbind(key: String, value: MacroKey): Map[String, String] = Map(key -> value.toString)
  }


  implicit object FunctionKeyFormatter extends Formatter[FunctionKey] {
    override val format: Option[(String, Nil.type)] = Some(("format.functionKey", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], FunctionKey] = parsing(s => FunctionKey(s.toInt), "error.url", Nil)(key, data)

    override def unbind(key: String, value: FunctionKey): Map[String, String] = Map(key -> value.number.toString)
  }

  implicit object DtmfFormatter extends Formatter[Dtmf] {
    override val format: Option[(String, Nil.type)] = Some(("format.dtmf", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Dtmf] = parsing(Dtmf(_), "error.url", Nil)(key, data)

    override def unbind(key: String, value: Dtmf): Map[String, String] = Map(key -> value.toString)
  }

  implicit val fmtMacroKey: OFormat[MacroKey] = Json.format[MacroKey]
  implicit val fmtLogicAlarmKey: OFormat[LogicAlarmKey] = Json.format[LogicAlarmKey]
  implicit val fmtAnalogAlarmKey: OFormat[MeterKey] = Json.format[MeterKey]
  implicit val fmtScheduleKey: OFormat[ScheduleKey] = Json.format[ScheduleKey]
  implicit val fmtCommonKey: OFormat[CommonKey] = Json.format[CommonKey]
  implicit val fmtFunctionKey: OFormat[FunctionKey] = Json.format[FunctionKey]
  implicit val fmtMessageMacroKey: OFormat[MessageKey] = Json.format[MessageKey]
  implicit val fmtCourtesyToneKey: OFormat[CourtesyToneKey] = Json.format[CourtesyToneKey]
  implicit val fmtPortKey: OFormat[PortKey] = Json.format[PortKey]
  implicit val fmtClockKey: OFormat[ClockKey] = Json.format[ClockKey]
  implicit val fmtDtmfMacroKey: OFormat[DtmfMacroKey] = Json.format[DtmfMacroKey]
  implicit val fmKey: OFormat[Key] = Json.format[Key]
  implicit val fmtNamedKey: OFormat[NamedKey] = Json.format[NamedKey]
  implicit val fmtTimerKey: OFormat[TimerKey] = Json.format[TimerKey]
}