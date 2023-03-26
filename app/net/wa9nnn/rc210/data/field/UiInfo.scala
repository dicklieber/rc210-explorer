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

import net.wa9nnn.rc210.data.Dtmf.dtmfDigits
import net.wa9nnn.rc210.data.field.FieldExtractors.{int16, int8, twoInts}

import scala.util.Try

trait UiInfo {
  val fieldExtractor: SimpleFieldExtractor
  val validate: String => Try[String]

  def doString(s: String): Try[String] = {
    validate(s)
  }

  val prompt: String = ""
  val unit: String = ""
}

object UiInfo {
  val macroSelect: UiInfo = new UiInfo{
    override val fieldExtractor: SimpleFieldExtractor = FieldExtractors.int8
    override val validate: String => Try[String] = (s: String) => Try(s)
  }

  val default: UiNumber = UiNumber(256, "")
  val checkBox: UiInfo = new UiInfo {
    override val fieldExtractor: SimpleFieldExtractor = FieldExtractors.bool
    val validate = (s: String) => Try(s)

    override def toString: String = "checkBox"
  }

  val twoNumbers: UiInfo = new UiInfo {
    val fieldExtractor: SimpleFieldExtractor = twoInts
    override val validate: String => Try[String] = (s: String) =>
      throw new NotImplementedError() //todo
    override val prompt = "<from macro> <to macro>"
  }

  val unlockCode: UiInfo = new UiInfo {
    override val fieldExtractor: SimpleFieldExtractor = DtmfExtractor(8)
    override val validate: String => Try[String] = (s: String) =>
      throw new NotImplementedError() //todo
    override val prompt = "1 to 8 digits"
  }
}

/**
 *
 * @param max  largest value.
 * @param unit e.g. seconds, minutes etc.
 */
case class UiNumber(max: Int, override val unit: String, tootltip: String = "") extends UiInfo {
  val fieldExtractor: SimpleFieldExtractor = if (max > 256) int16 else int8
  val validate: String => Try[String] = (s: String) => {
    val int = s.toInt
    Try {
      if (int > max) throw new IllegalArgumentException(s"Must be 1 to $max but found: $int")
      else
        s
    }
  }
  override val prompt: String = if (tootltip.isEmpty)
    s"1 to $max $unit"
  else
    tootltip
}

/**
 *
 * @param max  largest value.
 * @param unit e.g. seconds, minutes etc.
 */
case class UiRange(min: Int, max: Int, override val unit: String) extends UiInfo {
  val fieldExtractor: SimpleFieldExtractor = if (max > 256) int16 else int8
  val validate: String => Try[String] = (s: String) => {
    val int = s.toInt
    Try {
      if (int < min || int > max) throw new IllegalArgumentException(s"Must be $min to $max but found: $int")
      else
        s
    }
  }
  override val prompt = s"1 to $max $unit"
}

case class UiDtmf(max: Int) extends UiInfo {
  val fieldExtractor: SimpleFieldExtractor = DtmfExtractor(max)
  override val validate: String => Try[String] = (s: String) =>
    Try {
      if (s.length > max) throw new IllegalArgumentException(s"Must be 1 to $max digits but found: $s ${s.length}")
      else
        s
    }

  override val prompt = s"1 to $max digits"
}
case class UISelect(strings:(String,Int) *) extends UiInfo {
  override val fieldExtractor: SimpleFieldExtractor = FieldExtractors.int8
  override val validate: String => Try[String] = (s: String) =>
    //todo
    Try {
//      if (s.length > max) throw new IllegalArgumentException(s"Must be 1 to $max digits but found: $s ${s.length}")
//      else
        s
    }
}
