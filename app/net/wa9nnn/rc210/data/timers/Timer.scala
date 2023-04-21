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

import com.wa9nnn.util.tableui.{Cell, Row}
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldInt, RenderMetadata}
import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.key.KeyFactory.TimerKey
import net.wa9nnn.rc210.util.MacroSelect
import play.api.libs.json.{JsValue, Json, OFormat}

case class Timer(key: TimerKey, seconds: FieldInt, macroSelect: MacroSelect) extends ComplexFieldValue[TimerKey] {
  override val fieldName: String = "Timer"
  implicit val k: TimerKey = key

  override def toRow: Row = {
    val name: Cell = key.namedCell()

    Row(
      name,
      seconds.toCell(RenderMetadata("seconds", units = "Seconds")),
      macroSelect.toCell(RenderMetadata("macro")))
  }


  override def display: String = s"$seconds => ${macroSelect.value}"

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = "//todo"

  override def toJsonValue: JsValue = Json.toJson(this)
}

object Timer {

  import net.wa9nnn.rc210.key.KeyFormats._
  implicit val fmtTimer: OFormat[Timer] = Json.format[Timer]
}
