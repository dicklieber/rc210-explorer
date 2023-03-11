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
import net.wa9nnn.rc210.data.field.UiRender._

import scala.util.Try

trait UiInfo {
  val uiRender: UiRender
  val fieldExtractor: FieldExtractor
  val validate: String => Try[String]
  val selectOptions: Seq[SelectOption] = Seq.empty


  def doString(s: String): Try[String] = {
    validate(s)
  }

  val prompt: String = ""
}

object UiInfo {
  val default: UiNumber = UiNumber(256, "")
  val checkBox: UiInfo = new UiInfo {
    val uiRender = UiRender.checkbox
    override val fieldExtractor: FieldExtractor = FieldExtractors.bool
    val validate = (s: String) => Try(s)
    override val prompt = "true or false"

    override def toString: String = "checkBox"
  }

  val twoNumbers: UiInfo = new UiInfo {
    val uiRender: UiRender = UiRender.twoStrings
    val fieldExtractor: FieldExtractor = twoInts
    override val validate: String => Try[String] = (s: String) =>
      throw new NotImplementedError() //todo
    override val prompt = "<from macro> <to macro>"
  }

  val unlockCode: UiInfo = new UiInfo {
    override val uiRender = UiRender.dtmfKeys
    override val fieldExtractor: FieldExtractor = DtmfExtractor(8)
    override val validate: String => Try[String] = (s: String) =>
      throw new NotImplementedError() //todo
    override val prompt: String = "1 to 8 digits"
  }
}

/**
 *
 * @param max  largest value.
 * @param unit e.g. seconds, minutes etc.
 */
case class UiNumber(max: Int, unit: String) extends UiInfo {
  val uiRender: UiRender = number
  val fieldExtractor: FieldExtractor = if (max > 256) int16 else int8
  val validate: String => Try[String] = (s: String) => {
    val int = s.toInt
    Try {
      if (int > max) throw new IllegalArgumentException(s"Must be 1 to $max but found: $int")
      else
        s
    }
  }
  override val prompt = s"1 to $max $unit"
}

case class UiDtmf(max: Int) extends UiInfo {
  val uiRender: UiRender = dtmfKeys
  val fieldExtractor: FieldExtractor = DtmfExtractor(max)
  override val validate: String => Try[String] = (s: String) =>
    Try {
      if (s.length > max) throw new IllegalArgumentException(s"Must be 1 to $max digits but found: $s ${s.length}")
      else
        s
    }

  override val prompt: String = s"1 to $max digits"
}


/*
  case class UiSelect(fieldSelect: FieldSelect) extends UiInfo {
    val uiRender: field.UiRender.Value = UiRender.select
    val fieldExtractor: FieldExtractor = FieldExtractors.int8
    val validate: String => Try[String] = { (s: String) =>
      throw new NotImplementedError() //todo
    }
    override val prompt: String = "Choose from choices."

  }
*/
