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

import net.wa9nnn.rc210.data.field.schedule.{DayOfWeek, Week}
import net.wa9nnn.rc210.fixtures.WithMemory
import net.wa9nnn.rc210.key.KeyFactory

class ScheduleBuilderSpec extends WithMemory {
  val schedules: Seq[Schedule] = ScheduleBuilder(memory)

  "ScheduleBuilder" should {

    "Key number offset correct" in {
      schedules.head.key.rc210Value should equal(1)
      schedules.last.key.rc210Value should equal(40)
    }
    "Fields correct" in {
      val schedule3: Schedule = schedules(2)
      schedule3.key.rc210Value should equal(3)
      schedule3.dow should equal(DayOfWeek.EveryDay)
      schedule3.hour should equal(7)
      schedule3.minute should equal(0)
      schedule3.macroKey should equal(KeyFactory.macroKey(4))
    }
  }
}
