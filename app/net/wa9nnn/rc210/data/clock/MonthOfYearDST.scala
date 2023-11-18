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

package net.wa9nnn.rc210.data.clock

import net.wa9nnn.rc210.util.{SelectItemNumber, SelectableNumber}

sealed trait MonthOfYearDST(val rc210Value: Int, val display: String) extends SelectItemNumber

object MonthOfYearDST extends SelectableNumber[MonthOfYearDST] {
  case object January extends MonthOfYearDST(0, "January")

  case object February extends MonthOfYearDST(0, "February")

  case object March extends MonthOfYearDST(0, "March")

  case object April extends MonthOfYearDST(0, "April")

  case object May extends MonthOfYearDST(0, "May")

  case object June extends MonthOfYearDST(0, "June")

  case object July extends MonthOfYearDST(0, "July")

  case object August extends MonthOfYearDST(0, "August")

  case object September extends MonthOfYearDST(0, "September")

  case object October extends MonthOfYearDST(0, "October")

  case object November extends MonthOfYearDST(0, "November")

  case object December extends MonthOfYearDST(0, "December")

}


