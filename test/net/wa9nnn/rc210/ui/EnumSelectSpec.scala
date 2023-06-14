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

package net.wa9nnn.rc210.ui

import com.wa9nnn.util.tableui.Cell
import net.wa9nnn.RcSpec
import net.wa9nnn.rc210.data.field.Week
import net.wa9nnn.rc210.key.KeyFactory

class EnumSelectSpec extends RcSpec {
  implicit val k = KeyFactory.defaultMacroKey
  val enumSelect = new EnumSelect[Week]("name")
  "EnumSelectSpec" should {
    "apply no selected" in {
      val cell = enumSelect.toCell()
      val html = cell.value
      html.trim startsWith """<select name="theWeek" class="form-select" aria-label="Default select example">"""

    }
    "apply with selected" in {
      val cell: Cell = enumSelect.toCell( Week.first)
      val html: String = cell.value
      val bool = html.contains("<option value=\"first\" selected>first</option>")
      bool should equal(true)
    }

    "fromForm" in {
      val week = enumSelect.fromForm("first")
      week should equal (Week.first)
    }
  }
}
