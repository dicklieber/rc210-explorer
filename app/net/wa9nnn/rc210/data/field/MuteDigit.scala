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

import net.wa9nnn.rc210.util.select.{Rc210Item, SelectableNumber}
import play.api.libs.json.JsValue

sealed trait MuteDigit(val rc210Value: Int, val display: String) extends Rc210Item

object MuteDigit extends SimpleExtractor with SelectableNumber[MuteDigit] {

  case object firstDigit extends MuteDigit(1, "1st digit")

  case object secpondtDigit extends MuteDigit(2, "2nd digit")

  override def parse(jsValue: _root_.play.api.libs.json.JsValue): _root_.net.wa9nnn.rc210.data.field.FieldValue =
    throw new NotImplementedError() //todo
}
