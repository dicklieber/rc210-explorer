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

import net.wa9nnn.rc210.RcSpec
import net.wa9nnn.rc210.data.clock.{MonthOfYearDST, Occurrence}
import net.wa9nnn.rc210.data.clock.Occurrence.*

class FormFieldTest extends RcSpec {

  "FormFieldTest" when {
    "checkbox" should {
      "boolean checked" in {
        FormField("abool", true) shouldBe ("""<input name="abool" id="abool" checked="checked" type="checkbox"></input>""")
      }
      "boolean unchecked" in {
        assert(FormField("abool", false) == """<input name="abool" id="abool" type="checkbox"></input>""")
      }
    }
    "input" should {
      "string" in {
        val html: String = FormField("text", "Hello")
        assert(html == """<input name="text" id="text" type="text" value="Hello"></input>""")
      }
      "int" in {
        val html: String = FormField("anInt", 42)
        assert(html == """<input name="anInt" id="anInt" type="number" value="42"></input>""")
      }
    }
    "select" should {
      "string" in {
        val fifth = Occurrence.Fifth
        val values1 = fifth.values
        val april = MonthOfYearDST.February
        val html: String = FormField("text", april)
        html shouldBe ( """<select name="text" id="text">
                         |          <option value="January">
                         |            January
                         |          </option><option value="February" selected="selected">
                         |            February
                         |          </option><option value="March">
                         |            March
                         |          </option><option value="April">
                         |            April
                         |          </option><option value="May">
                         |            May
                         |          </option><option value="June">
                         |            June
                         |          </option><option value="July">
                         |            July
                         |          </option><option value="August">
                         |            August
                         |          </option><option value="September">
                         |            September
                         |          </option><option value="October">
                         |            October
                         |          </option><option value="November">
                         |            November
                         |          </option><option value="December">
                         |            December
                         |          </option>
                         |        </select>
                         |"""".stripMargin)}
    }
  }
}
