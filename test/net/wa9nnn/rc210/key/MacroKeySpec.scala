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

package net.wa9nnn.rc210.key

import net.wa9nnn.rc210.fixtures.WithMemory
import net.wa9nnn.rc210.key.KeyFactory.MacroKey
import play.api.libs.json.Json

class MacroKeySpec extends WithMemory {

  "MacroKeySpec" should {
    "number" in {
      val macroKey = MacroKey(42)
      macroKey.toString must beEqualTo ("macroKey42")
    }

    "round trip" in {
      import KeyFormats._
      val macroKey = MacroKey(42)
      val sJson = Json.prettyPrint( Json.toJson(macroKey))
      val backAgain = Json.parse(sJson).as[MacroKey]
      backAgain must beEqualTo (macroKey)
    }
  }
}
