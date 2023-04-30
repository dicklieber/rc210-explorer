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

package net.wa9nnn.rc210.data.schedules

import net.wa9nnn.rc210.data.field.{DayOfWeek, FieldBoolean, FieldTime, MonthOfYear, WeekInMonth}
import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.key.KeyFactory.FunctionKey
import net.wa9nnn.rc210.util.MacroSelect
import org.specs2.mutable.Specification

import java.time.LocalTime

class ScheduleSpec extends Specification {

  "Schedule" should {
    "toCommands" in {
      val schedule: Schedule = Schedule(
        key = KeyFactory.ScheduleKey(1),
        dayOfWeek = new DayOfWeek(),
        weekInMonth = new WeekInMonth(),
        monthOfYear = new MonthOfYear(),
        time = FieldTime(LocalTime.of(10, 15)),
        selectedMacroToRun = MacroSelect(KeyFactory.defaultMacroKey),
        enabled = FieldBoolean
      )
      ok
    }
  }
}
