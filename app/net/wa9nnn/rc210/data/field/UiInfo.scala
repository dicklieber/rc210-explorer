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
import net.wa9nnn.rc210.data.field.FieldExtractors.{dtmf, int16, int8, twoInts, unlock}
import net.wa9nnn.rc210.data.field.UiRender._

import scala.util.Try

abstract class UiInfo(val uiRender: UiRender, val fieldExtractor: FieldExtractor, validate: String => Try[String]) {
  def doString(s: String): Try[String] = {
    validate(s)
  }

  val prompt: String
}

object UiInfo {
  val default: UiNumber = UiNumber(256, "")
  val checkBox: UiInfo = new UiInfo(uiRender = UiRender.checkbox,
    FieldExtractors.bool,
    validate = (s: String) => Try(s)) {
    override val prompt = "true or false"
  } // always valid.

  override def toString: String = "checkBox"

  val unlockCode: UiUnlockCode = UiUnlockCode()

}


case class UiNumber(max: Int, unit: String) extends UiInfo(
  uiRender = number,
  fieldExtractor = if (max > 256) int16 else int8,
  validate = (s: String) => {
    val int = s.toInt
    Try {
      if (int > max) throw new IllegalArgumentException(s"Must be 1 to $max but found: $int")
      else
        s
    }
  }
) {

  override val prompt = s"1 to $max $unit"
}

case class UiDtmf(max: Int) extends UiInfo(uiRender = dtmfKeys, fieldExtractor = dtmf, validate = (s: String) => {
  Try {
    if (s.length > max) throw new IllegalArgumentException(s"Must be 1 to $max digits but found: $s ${s.length}")
    else
      s
  }
}) {

  override val prompt: String = s"1 to $max digits"
}

case class UiTwoNumbers(sPrompt: String) extends UiInfo(
  uiRender = UiRender.twoStrings,
  fieldExtractor = twoInts,
  validate = { (s: String) =>
    throw new NotImplementedError() //todo
  }
) {
  override val prompt: String = sPrompt

}
case class UiUnlockCode() extends UiInfo(
  uiRender = UiRender.number,
  fieldExtractor = unlock,
  validate = { (s: String) =>
    throw new NotImplementedError() //todo
  }
) {
  override val prompt: String = "1 to 8 digits"

}
