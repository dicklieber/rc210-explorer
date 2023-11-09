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

package net.wa9nnn.rc210.data.logicAlarm

import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.{Format, JsValue, Json}
import net.wa9nnn.rc210.key.KeyFormats.*
import net.wa9nnn.rc210.key._

case class LogicAlarm(key: LogicAlarmKey, enable: Boolean, lowMacro: MacroKey, highMacro: MacroKey) extends ComplexFieldValue[LogicAlarmKey] {
  override val fieldName: String = LogicAlarm.name

  override def display: String = toString

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = Seq {
    "todo"
  }

  override def toJsonValue: JsValue = Json.toJson(this)
}

object LogicAlarm extends ComplexExtractor[LogicAlarmKey] {
  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val enables = memory.sub8(169, 5).map(_ != 0)
    val lowMacros = memory.sub8(174, 5).map(n => KeyFactory.macroKey(n + 1))
    val highMacros = memory.sub8(179, 5).map(n => KeyFactory.macroKey(n + 1))
    //    SimpleField(169, "Enable", logicAlarmKey, "1n91b", FieldBoolean)
    //    SimpleField(174, "Macro Low", logicAlarmKey, "1*2101nv", MacroSelectField)
    //    SimpleField(179, "Macro High", logicAlarmKey, "1*2102nv", MacroSelectField)

    for {
      i <- 0 until KeyKind.logicAlarmKey.maxN
    } yield {
      val logicAlarmKey = KeyFactory.LogicAlarmKey(i + 1)
      val fieldValue: LogicAlarm = new LogicAlarm(logicAlarmKey, enables(i), lowMacros(i), highMacros(i))
      FieldEntry(this, fieldValue)
    }
  }
  //  def apply(valuesMap: Map[String, String]):LogicAlarm = {
  //    throw new NotImplementedError() //todo
  //  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[LogicAlarm]

  /**
   * for various things e.g. parser name.
   */
  override val name: String = "LogicAlarm"
  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.logicAlarmKey

  override def positions: Seq[FieldOffset] = Seq(

  )


  implicit val fmtLogicAlarm: Format[LogicAlarm] = Json.format[LogicAlarm]


  override def parseFormFields[T <: ComplexFieldValue[LogicAlarmKey]](valuesMap: Map[String, String]): ComplexFieldValue[LogicAlarmKey] = {
    throw new NotImplementedError() //todo
  }


}
