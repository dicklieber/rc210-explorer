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

import net.wa9nnn.rc210.util.{SelectItem, SelectItemNumber, SelectableNumber}
import play.api.data.FormError
import play.api.data.format.Formats.*
import play.api.data.format.Formatter
import play.api.libs.json.{Format, Json}


sealed trait Radio(val rc210Value: Int, val display: String) extends SelectItemNumber

object Radio extends SelectableNumber[Radio]:
  case object Kenwood extends Radio(1, "Kenwood")

  case object Icom extends Radio(2, "Icom")

  case object Yaesu extends Radio(3, "Yaesu")

  case object KenwoodV7 extends Radio(4, "Kenwood V7")

  case object DougHallRBI1 extends Radio(5, "Doug Hall RBI-1")

  //no 6
  case object KenwoodG707 extends Radio(7, "Kenwood g707")

  case object Kenwood271A extends Radio(8, "Kenwood 271A")

  case object KenwoodV71a extends Radio(9, "Kenwood V71a")

sealed trait Yaesu(val rc210Value: Int, val display: String) extends SelectItemNumber
object Yaesu extends SelectableNumber[Yaesu]:
  case object FT100D extends Radio(1, "FT-100D")
  case object FT817_857_897 extends Radio(2, "FT817, FT-857, FT-897")
  case object FT847 extends Radio(3, "FT847")


sealed trait Offset(val rc210Value: Int, val display: String) extends SelectItemNumber
object Offset extends SelectableNumber[Offset]:
  case object FT100D extends Radio(1, "Minus")

  case object FT817_857_897 extends Radio(2, "Simplex")

  case object FT847 extends Radio(3, "Plus")

sealed trait Mode(val rc210Value: Int, val display: String) extends SelectItemNumber
object Mode extends SelectableNumber[Mode]:
  case object LSB extends Radio(1, "LSB")
  case object USB extends Radio(2, "USB")
  case object CW extends Radio(3, "CW")


sealed trait CtcssMode(val rc210Value: Int, val display: String) extends SelectItemNumber
object CtcssMode extends SelectableNumber[CtcssMode]:
  case object None extends Radio(0, "None")
  case object EncodeOnly extends Radio(1, "Encode Only")
  case object EncodeDecode extends Radio(2, "Encode/Decode")

sealed trait CtcssTone(val rc210Value: Int, val display: String) extends SelectItemNumber
object CtcssTone extends SelectableNumber[CtcssTone]:
  case object _00 extends CtcssTone(0, "todo")

