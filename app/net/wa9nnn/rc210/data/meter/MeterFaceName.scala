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

package net.wa9nnn.rc210.data.meter

import net.wa9nnn.rc210.ui.{EnumEntryValue, EnumValue}


sealed abstract class MeterFaceName(val rc210Value: Int) extends EnumEntryValue

object MeterFaceName extends EnumValue[MeterFaceName] {
  override val values: IndexedSeq[MeterFaceName] = findValues

  case object Off extends MeterFaceName(0)

  case object Volts extends MeterFaceName(1)

  case object Amps extends MeterFaceName(2)

  case object Watts extends MeterFaceName(3)

  case object Degrees extends MeterFaceName(4)

  case object MPH extends MeterFaceName(5)

  case object Percent extends MeterFaceName(6)


}

