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

import net.wa9nnn.rc210.data.field.FieldExtractors.{int16, int8}
import net.wa9nnn.rc210.data.field.UiRender.{UiRender, dtmf, number}

import scala.util.Try

class UiInfo(val rend: UiRender, val fieldExtractor:FieldExtractor, val options: Option[FieldSelect] = None, validate: String => Try[String]) {
  def doString(s: String): Try[String] = {
    validate(s)
  }
}

object UiInfo {
  val default: UiNumber =  UiNumber(256)
  val checkBox: UiInfo = new UiInfo(rend = UiRender.checkbox,
    FieldExtractors.bool,
    validate = (s: String) => Try(s)) // always valid.

}

case class UiNumber(max: Int) extends UiInfo(
  rend = number,
  fieldExtractor = if(max > 255) int16 else int8,
  validate = (s: String) => {
  val int = s.toInt
  Try {
    if (int > max) throw new IllegalArgumentException(s"Must be 1 to $max but found: $int")
    else
      s
  }
})
case class UiDtmf(max: Int) extends UiInfo(rend = dtmf, validate = (s: String) => {
  Try {
    if (s.length > max) throw new IllegalArgumentException(s"Must be 1 to $max digits but found: $s ${s.length}")
    else
      s
  }
})


object UiRender extends Enumeration {
  type UiRender = Value
  val checkbox, number, select, dtmf = Value
}