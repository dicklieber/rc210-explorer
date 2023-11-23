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

package net.wa9nnn.rc210.data.timers

import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntryBase}
import play.api.libs.json.{JsValue, Json, OFormat}

case class Timer(key: Key, seconds: Int, macroKey: Key) extends ComplexFieldValue("Timer") {

  override def display: String = s"$seconds => ${macroKey.keyWithName}"

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val timeNumber: Int = key.rc210Value
    val secs: Int = seconds
    val macroNumber = macroKey.rc210Value
    Seq(
      s"1*1017$timeNumber$secs",
      s"1*2092$timeNumber$macroNumber"
    )
  }

  override def toJsonValue: JsValue = Json.toJson(this)
}

object Timer {

  implicit val fmtTimer: OFormat[Timer] = Json.format[Timer]
}
