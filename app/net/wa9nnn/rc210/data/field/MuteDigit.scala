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

import net.wa9nnn.rc210.ui.Rc210Enum

sealed trait MuteDigit(val rc210Value: Int) extends EnumEntryFieldValue

object MuteDigit extends Rc210Enum[MuteDigit]:
  override def values: Seq[MuteDigit] = MuteDigit.values

  case object firstDigit extends MuteDigit(1)

  case object secondDigit extends MuteDigit(2)

