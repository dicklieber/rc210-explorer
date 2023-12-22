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

package net.wa9nnn.rc210

import play.api.libs.json.{Format, Json}

class KeyTest extends WithMemory {
  val macroKey3 = Key(KeyKind.RcMacro, 3)
  "Happy" in {
    macroKey3.toString mustBe ("macroKey3")
  }
  "round trip toString apply" in {
    val string = macroKey3.toString
    val backAgain = Key(string)
    backAgain mustBe (macroKey3)
  }
  "round trip JSON" in {
    val container = KeyContainer(macroKey3)
    val sJson = Json.prettyPrint(Json.toJson(container))
    sJson mustBe ("""{
                      |  "key" : "macroKey3",
                      |  "other" : 42
                      |}""".stripMargin)
    val backAgain = Json.parse(sJson).as[KeyContainer]
    backAgain mustBe (container)
  }
  "throw if too big a number" in {
    assertThrows[AssertionError] { // Result type: Assertion
      Key(KeyKind.Meter, 42)
    }
  }
  "macroKeys" in {
    val keys = Key.macroKeys
    keys must have length KeyKind.RcMacro.maxN
  }
}

case class KeyContainer(key: Key, other: Int = 42)

implicit val fmtKeyContainer: Format[KeyContainer] = Json.format[KeyContainer]
