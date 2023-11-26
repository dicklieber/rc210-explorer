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

package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.data.field.FieldKey
import net.wa9nnn.rc210.util.select.EnumEntryValue

import java.time.LocalTime
import scala.xml.*

/**
 * Generates  html elements for any value type.
 * [[https://github.com/scala/scala-xml Scala-xml]]
 */
object FormField:
  def apply(fieldKey: FieldKey, value: Any): String =
    apply(fieldKey.toString, value)

  def apply(name: String, value: Any): String =
    ((value match {
      case enumValue: EnumEntryValue =>
        <select>
          {enumValue.values map { choice =>
          val opt = <option value={choice.toString} selected={if (enumValue == choice) "selected" else null}>
            {choice.toString}
          </option>
          opt
        }}
        </select>

      case b: Boolean =>
        val elem = <input type="checkbox"></input>
        if (b)
          elem % Attribute(None, "checked", Text("checked"), Null)
        else
          elem

      case int: Int =>
        <input type="number" value={int.toString}></input>

      case s: String =>
        <input type="text" value={s}></input>

      case localTime: LocalTime =>
        <input type="time" class="timepicker" value={localTime.toString}></input>

      case x =>
        <span>
          {x.toString}
        </span>
    })
      // These get added to any generated html.
      % Attribute(None, "id", Text(name), Null)
      % Attribute(None, "name", Text(name), Null))
      .toString

