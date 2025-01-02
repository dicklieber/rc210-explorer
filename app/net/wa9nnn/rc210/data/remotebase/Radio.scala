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

package net.wa9nnn.rc210.data.remotebase

import net.wa9nnn.rc210.data.field.Rc210EnumEntry
import net.wa9nnn.rc210.data.remotebase.Radio.values
import net.wa9nnn.rc210.ui.Rc210Enum

sealed trait Radio(val rc210Value: Int, val display: String) extends Rc210EnumEntry:
  override val vals: Seq[Rc210EnumEntry] = values

object Radio extends Rc210Enum[Radio]:

  override val values: IndexedSeq[Radio] = findValues

  case object Kenwood extends Radio(1, "Kenwood")

  case object Icom extends Radio(2, "Icom")

  case object Yaesu extends Radio(3, "Yaesu")

  case object KenwoodV7 extends Radio(4, "Kenwood V7")

  case object DougHallRBI1 extends Radio(5, "Doug Hall RBI-1")

  //no 6
  case object KenwoodG707 extends Radio(7, "Kenwood g707")

  case object Kenwood271A extends Radio(8, "Kenwood 271A")

  case object KenwoodV71a extends Radio(9, "Kenwood V71a")


