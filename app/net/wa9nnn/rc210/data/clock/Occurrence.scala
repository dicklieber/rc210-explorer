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

import enumeratum.*
import net.wa9nnn.rc210.util.select.{SelectBase, SelectItemNumber, SelectableNumber}


sealed trait Occurrence(val rc210Value: Int, val display: String) extends  SelectItemNumber

object Occurrence extends SelectBase[Occurrence] {

  case object First extends Occurrence(1, "First")

  case object Second extends Occurrence(2, "Second")

  case object Third extends Occurrence(3, "Third")

  case object Forth extends Occurrence(4, "Forth")

  case object Fifth extends Occurrence(5, "Fifth")
}