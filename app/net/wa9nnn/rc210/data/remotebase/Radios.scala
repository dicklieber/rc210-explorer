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

import net.wa9nnn.rc210.util.{SelectItem, SelectItemNumber, Selectable}
import play.api.data.FormError
import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.libs.json.{Format, Json}


case class Radio(number: Int, display: String) extends SelectItemNumber

case class Yaesu(number: Int, display: String) extends SelectItemNumber


object Radios extends Selectable[Radio](
  Radio(1, "Kenwood"),
  Radio(2, "Icom"),
  Radio(3, "Yaesu"),
  Radio(4, "Kenwood V7"),
  Radio(5, "Doug Hall RBI-1"),
  // no 6
  Radio(7, "Kenwood g707"),
  Radio(8, "Kenwood 271A"),
  Radio(9, "Kenwood V71a"),
)

object Yaesus extends Selectable[Yaesu](
  Yaesu(1, "FT-100D"),
  Yaesu(2, "FT817, FT-857, FT-897"),
  Yaesu(3, "FT847"),
)


case class Offset(number: Int, display: String) extends SelectItemNumber

object Offsets extends Selectable[Offset](
  Offset(1, "Minus"),
  Offset(2, "Simplex"),
  Offset(3, "Plus"),
)

case class Mode(number: Int, display: String) extends SelectItemNumber

object Modes extends Selectable[Mode](
  Mode(1, " LSB"),
  Mode(2, " USB"),
  Mode(3, " CW"),
  Mode(4, " FM"),
  Mode(5, " AM"),
)

case class CtcssMode(number: Int, display: String) extends SelectItemNumber

object CtcssModes extends Selectable[CtcssMode](
  CtcssMode(0, "None"),
  CtcssMode(1, "Encode Only"),
  CtcssMode(2, "Encode/Decode"),
)


case class CtcssTone(number: Int, display: String) extends SelectItemNumber

object CtcssTones extends Selectable[CtcssTone]() //todo add options 

