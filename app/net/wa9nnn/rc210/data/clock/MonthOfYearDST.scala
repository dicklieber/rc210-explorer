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

import net.wa9nnn.rc210.data.clock.MonthOfYearDST.values
import net.wa9nnn.rc210.data.field.Rc210EmumEntry
import net.wa9nnn.rc210.ui.Rc210Enum


sealed trait  MonthOfYearDST(val rc210Value: Int) extends Rc210EmumEntry:
  override val vals: Seq[Rc210EmumEntry] = values

object MonthOfYearDST extends Rc210Enum[MonthOfYearDST] :
  override val values: IndexedSeq[MonthOfYearDST] = findValues

  case object January extends MonthOfYearDST(0)

  case object February extends MonthOfYearDST(1)

  case object March extends MonthOfYearDST(2)

  case object April extends MonthOfYearDST(3)

  case object May extends MonthOfYearDST(4)

  case object June extends MonthOfYearDST(5)

  case object July extends MonthOfYearDST(6)

  case object August extends MonthOfYearDST(7)

  case object September extends MonthOfYearDST(8)

  case object October extends MonthOfYearDST(9)

  case object November extends MonthOfYearDST(10)

  case object December extends MonthOfYearDST(11)


