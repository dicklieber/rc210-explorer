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

package net.wa9nnn.rc210.util

import com.wa9nnn.util.tableui.Cell
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.RenderMetadata
import net.wa9nnn.rc210.key.KeyFactory
import org.specs2.mutable.Specification

class MacroSelectSpec extends Specification {
  private val renderMetadata = new RenderMetadata {
    override def param = FieldKey("to Run", KeyFactory.defaultMacroKey).param

    override def prompt = "macro"

    override def units = ""
  }
  "MacroSelect" should {
    val macroSelect =new  MacroSelect()
    "initial state" >> {
      macroSelect.value must beEqualTo(KeyFactory.defaultMacroKey)

    }

    "toHtmlField" in {
      val html = macroSelect.toHtmlField(renderMetadata)
      html must contain("<option value=\"macroKey1\"   >1 </option>")

    }

  }
}
