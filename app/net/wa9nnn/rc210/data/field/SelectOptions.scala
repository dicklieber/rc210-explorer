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

import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.key.{KeyKind, MacroKey}

object SelectOptions {
  val dayOfWeek: FieldSelect = FieldSelect(
    "EveryDay",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
    "Sunday",
    "Weekdays",
    "Weekends"
  )
  val dtmfMuteDigit: FieldSelect = new FieldSelect(
    Seq(SelectOption(1, "1st digit"),
      SelectOption(2, "2ndt digit"))
  )
  val radioType: FieldSelect = new FieldSelect(
    Seq(
      SelectOption(1, "Kenwood"),
      SelectOption(2, "Icom"),
      SelectOption(3, "Yaesu"),
      SelectOption(4, "Kenwood V7a"),
      SelectOption(5, "Doug Hall RBI - 1"),
      SelectOption(7, "Kenwood g707"),
      SelectOption(8, "Kenwood 271 A"),
      SelectOption(9, "Kenwood V71a")
    )
  )
  val yaesuType: FieldSelect = new FieldSelect(
    Seq(
      SelectOption(1, "FT-100D"),
      SelectOption(2, "FT817, FT-857, FT-897"),
      SelectOption(3, "FT847"),
    )
  )

  val macroSelect: FieldSelect = new FieldSelect(Seq.empty) {
    override def options(namedSource: NamedSource) = {
      for {
        number <- 1 to KeyKind.macroKey.getMaxN
        macroKey = MacroKey(number)

      } yield {
        SelectOption(number, namedSource(macroKey))
      }
    }
  }
}
