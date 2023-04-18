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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, portKey}
import net.wa9nnn.rc210.key.KeyKind
import org.specs2.mutable.Specification

class FieldIntSpec extends Specification {
implicit val key: MacroKey = MacroKey(3)
  "FieldInt" should {
    "toHtmlField" in {
      val fieldInt = FieldInt(42)
      fieldInt.value must beEqualTo (42)

      val html: String = fieldInt.toHtmlField(RenderMetadata("groucho", units = "cigars"))
      html.dropWhile(_ != '<') must beEqualTo ("""<div class="valueCell">
                                                 |            <input id="groucho:macroKey3"  name="groucho:macroKey3" value="42" title="">
                                                 |            <span class="units">cigars</span>
                                                 |        </div>
                                                 |    """.stripMargin)
    }

    "toCell" in {
      ok
    }
  }
  "command" >> {
    val fieldInt = FieldInt(42)
    val candidate = FieldInt(142)
    val fieldDefinition = SimpleField(17, "Hang Time 3", KeyKind.portKey, "n*10003v", FieldInt)
    val fieldKey: FieldKey = fieldDefinition.fieldKey(3)
    val fieldEntry = FieldEntry(fieldDefinition, fieldKey, fieldInt, Option(candidate))
    val command = fieldEntry.toCommand
    command must beEqualTo ("3*10003142")
    //                       +============== Port 1,2, or 3
    //                        +++++========= Commnd base e.g. *1000
    //                             +======= Hang time number 1,2 or 3
    //                              +++====== 1/10 of seconds
  }

  "display" >> {
    val fieldInt = FieldInt(42)
    fieldInt.display must beEqualTo ("42")
  }
  "json" >> {
    val fieldInt = FieldInt(42)
    fieldInt.toJsonValue.as[Int] must beEqualTo (42)
  }
}
