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

package net.wa9nnn.rc210.data.field.schedule

import net.wa9nnn.rc210.util.{SelectItemNumber, SelectableNumber}


sealed trait DayOfWeek(val rc210Value: Int, val display: String) extends SelectItemNumber

object DayOfWeek extends SelectableNumber[DayOfWeek] {

  case object EveryDay extends DayOfWeek(1, "EveryDay")

  case object Monday extends DayOfWeek(2, "Monday")

  case object Tuesday extends DayOfWeek(3, "Tuesday")

  case object Wednesday extends DayOfWeek(4, "Wednesday")

  case object Thursday extends DayOfWeek(5, "Thursday")

  case object Friday extends DayOfWeek(6, "Friday")

  case object Saturday extends DayOfWeek(7, "Saturday")

  case object Sunday extends DayOfWeek(8, "Sunday")

  case object Weekdays extends DayOfWeek(9, "Weekdays")

  case object Weekends extends DayOfWeek(10, "Weekends")
}
