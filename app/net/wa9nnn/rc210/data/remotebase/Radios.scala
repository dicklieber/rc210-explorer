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


case class Radio(value: Int, display: String) extends SelectItemNumber

case class Yaesu(value: Int, display: String) extends SelectItemNumber


object Radio extends Selectable[Radio] {
  //  def apply(number: Int): Radio =
  //    lookup(number)
  //
  //  def apply(s: String): Radio = {
  //    choices.find(_.display == s).getOrElse(choices.head)
  //  }

  val choices: Seq[Radio] = Seq(
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

  implicit val fmtRadio: Format[Radio] = Json.format[Radio]
}


object Yaesu extends Selectable[Yaesu] {
  val choices: Seq[Yaesu] = Seq(
    Yaesu(1, "FT-100D"),
    Yaesu(2, "FT817, FT-857, FT-897"),
    Yaesu(3, "FT847"),
  )
  implicit val fmtYaesu: Format[Yaesu] = Json.format[Yaesu]
}

case class Offset(value: Int, display: String) extends SelectItemNumber

object Offset extends Selectable[Offset] {
  val choices: Seq[Offset] = Seq(
    Offset(1, "Minus"),
    Offset(2, "Simplex"),
    Offset(3, "Plus"),
  )
  implicit val fmtOffset: Format[Offset] = Json.format[Offset]
}

case class Mode(value: Int, display: String) extends SelectItemNumber

object Mode extends Selectable[Mode] {
  val choices: Seq[Mode] = Seq(
    Mode(1, " LSB"),
    Mode(2, " USB"),
    Mode(3, " CW"),
    Mode(4, " FM"),
    Mode(5, " AM"),
  )
  implicit val fmtMode: Format[Mode] = Json.format[Mode]
}

case class CtcssMode(value: Int, display: String) extends SelectItemNumber

object CtcssMode extends Selectable[CtcssMode] {
  val choices: Seq[CtcssMode] = Seq(
    CtcssMode(0, "None"),
    CtcssMode(1, "Encode Only"),
    CtcssMode(2, "Encode/Decode"),
  )

  implicit val fmtCtcssMode: Format[CtcssMode] = Json.format[CtcssMode]
}

case class CtcssTone(value: Int, display: String) extends SelectItemNumber

object CtcssTone extends Selectable {
  val choices: Seq[CtcssTone] = Seq.empty
  implicit val fmtCtcssTone: Format[CtcssTone] = Json.format[CtcssTone]
}

