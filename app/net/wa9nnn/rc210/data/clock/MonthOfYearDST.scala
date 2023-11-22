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
import enumeratum.PlayEnum
import enumeratum.values.*
import net.wa9nnn.rc210.util.select.{EnumEntryValue, EnumValue}

sealed abstract class  MonthOfYearDST(val rc210Value: Int, val display: String) extends EnumEntryValue

object MonthOfYearDST extends EnumValue[MonthOfYearDST] {
  val values = findValues

  case object January extends MonthOfYearDST(0, "January")

  case object February extends MonthOfYearDST(1, "February")

  case object March extends MonthOfYearDST(2, "March")

  case object April extends MonthOfYearDST(3, "April")

  case object May extends MonthOfYearDST(4, "May")

  case object June extends MonthOfYearDST(5, "June")

  case object July extends MonthOfYearDST(6, "July")

  case object August extends MonthOfYearDST(7, "August")

  case object September extends MonthOfYearDST(8, "September")

  case object October extends MonthOfYearDST(9, "October")

  case object November extends MonthOfYearDST(10, "November")

  case object December extends MonthOfYearDST(11, "December")

}


