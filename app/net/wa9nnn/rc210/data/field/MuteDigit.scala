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

import net.wa9nnn.rc210.data.field.MuteDigit.values
import net.wa9nnn.rc210.ui.{Rc210Enum, Rc210EnumEntry}
//*2090x where x=1 to mute on the 1st digit or x=2 to mute on the 2nd digit
sealed abstract class MuteDigit(val rc210Value: Int) extends Rc210EnumEntry:
  override val vals: Seq[Rc210EnumEntry] = values
  logger.trace(s"vals: $vals")

object MuteDigit extends Rc210Enum[MuteDigit]:

  override val values: IndexedSeq[MuteDigit] = findValues

  case object FirstDigit extends MuteDigit(1)

  case object SecondDigit extends MuteDigit(2)

