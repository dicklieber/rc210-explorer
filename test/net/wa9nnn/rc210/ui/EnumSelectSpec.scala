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
import net.wa9nnn.rc210.data.field.Week
import org.specs2.mutable.Specification

class EnumSelectSpec extends Specification {

  val enumSelect = new EnumSelect[Week]("theWeek", Week.values())
  "EnumSelectSpec" should {
    "apply no selected" in {
      val cell = enumSelect.toCell
      val html = cell.value
      html.trim startsWith """<select name="theWeek" class="form-select" aria-label="Default select example">"""

    }
    "apply wth selected" in {
      val cell: Cell = enumSelect.toCell(Week.first)
      val html = cell.value
      html must contain("<option value=\"first\" selected>first</option>")
    }

    "fromForm" in {
      val week = enumSelect.fromForm("first")
      week must beEqualTo(Week.first)
    }
  }
}
