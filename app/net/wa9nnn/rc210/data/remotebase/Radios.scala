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

import net.wa9nnn.rc210.util.SelectOption
import play.api.data.FormError
import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.libs.json.{Format, Json}


trait RemoteThing {
  val number: Int
  val display: String

//  def choice: (String, String) = display -> display
}

case class Radio(number: Int, display: String) extends RemoteThing

case class Yaesu(number: Int, display: String) extends RemoteThing

abstract class Selectable[T <: RemoteThing] {
  val choices: Seq[RemoteThing]

  def options: Seq[(String, String)] = choices.map(rt => rt.display -> rt.display)
  //  def options: Seq[SelectOption] = {
  //    choices.map { radio =>
  //      SelectOption(radio.display)
  //    }
  //  }

  def lookup[T <: RemoteThing](number: Int): T = {
    val t: T = choices.find(_.number == number).getOrElse(choices.head).asInstanceOf[T]
    t
  }


}

object Radio extends Selectable[Radio] {
  def apply(number: Int): Radio =
    lookup(number)

  def apply(s: String): Radio = {
    choices.find(_.display == s).getOrElse(choices.head)
  }

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

  implicit object radioFormatter extends Formatter[Radio] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Radio] = {
      val r: Either[Seq[FormError], Radio] = parsing(Radio(_), "error.url", Nil)(key, data)
      r
    }


    override def unbind(key: String, radio: Radio): Map[String, String] = {
      Map(key -> radio.display)
    }
  }

  implicit val fmtRadio: Format[Radio] = Json.format[Radio]
}


object Yaesu extends Selectable[Yaesu] {
  val choices: Seq[Yaesu] = Seq(
    Yaesu(1, "FT-100D"),
    Yaesu(2, "FT817, FT-857, FT-897"),
    Yaesu(3, "FT847"),
  )
  implicit val fmtYaesu: Format[Yaesu] = Json.format[Yaesu]


  implicit object OffsetFormatter extends Formatter[Yaesu] {
    override def bind(key: String, data: Map[String, String]): Either[scala.Seq[FormError], Yaesu] =
      parsing(Yaesu(_), "error.url", Nil)(key, data)


    override def unbind(key: String, yaesu: Yaesu): Map[String, String] = {
      Map(key -> yaesu.display)
    }
  }

  def apply(number: Int): Yaesu = lookup(number)

  def apply(display: String): Yaesu = choices.find(_.display == display).getOrElse(choices.head)
}

case class Offset(number: Int, display: String) extends RemoteThing

object Offset extends Selectable[Offset] {
  val choices: Seq[Offset] = Seq(
    Offset(1, "Minus"),
    Offset(2, "Simplex"),
    Offset(3, "Plus"),
  )

  def apply(number: Int): Offset = lookup(number)
  def apply(s:String): Offset = choices.find(_.display == s).get

  implicit val fmtOffset: Format[Offset] = Json.format[Offset]

  implicit object offsetFormatter extends Formatter[Offset] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Offset] = {
      val r: Either[scala.Seq[FormError], Offset] = parsing(Offset(_), "error.url", Nil)(key, data)
      r
    }
    //      parsing(Offset(_), "error.url", Nil)(key, data)


    override def unbind(key: String, offset: Offset): Map[String, String] = {
      Map(key -> offset.display)
    }
  }
}

case class Mode(number: Int, display: String) extends RemoteThing {
}

object Mode extends Selectable[Mode] {
  val choices: Seq[Mode] = Seq(
    Mode(1, " LSB"),
    Mode(2, " USB"),
    Mode(3, " CW"),
    Mode(4, " FM"),
    Mode(5, " AM"),
  )

  def apply(number: Int): Mode = lookup(number)

  def apply(display: String): Mode = choices.find(_.display == display).getOrElse(choices.head)

  implicit object ModeFormatter extends Formatter[Mode] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Mode] = parsing(s => Mode(s), "error.url", Nil)(key, data)

    override def unbind(key: String, value: Mode): Map[String, String] = {
      Map(key -> value.display)
    }

  }

  implicit val fmtMode: Format[Mode] = Json.format[Mode]
}

case class CtcssMode(number: Int, display: String) extends RemoteThing

object CtcssMode extends Selectable[CtcssMode] {
  val choices: Seq[CtcssMode] = Seq(
    CtcssMode(0, "None"),
    CtcssMode(1, "Encode Only"),
    CtcssMode(2, "Encode/Decode"),
  )

  implicit object CtcssModeFormatter extends Formatter[CtcssMode] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], CtcssMode] = parsing(s => CtcssMode(s), "error.url", Nil)(key, data)

    override def unbind(key: String, value: CtcssMode): Map[String, String] = {
      Map(key -> value.display)
    }
  }

  def apply(number: Int): CtcssMode = lookup(number)

  def apply(display: String): CtcssMode = choices.find(_.display == display).getOrElse(choices.head)

  implicit val fmtCtcssMode: Format[CtcssMode] = Json.format[CtcssMode]
}

case class CtcssTone(number: Int, display: String) extends RemoteThing

object CtcssTone extends Selectable {
  val choices: Seq[CtcssTone] = Seq.empty
  implicit val fmtCtcssTone: Format[CtcssTone] = Json.format[CtcssTone]
}

