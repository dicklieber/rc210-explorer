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

package net.wa9nnn.rc210.data.functions

import net.wa9nnn.rc210.{Functions, Key, KeyKind, RcSpec}

class FunctionsTest extends RcSpec {
  "FunctionsTest" should {

    "size" in {
      Functions.size mustBe(872)
    }


    "happy path" in {
      val maybeNode = Functions.maybeFunctionNode(Key(KeyKind.Function, 3))
      maybeNode
    }
    "wrong key type" in {
      assertThrows[IllegalArgumentException] { // Result type: Assertion
        Functions.maybeFunctionNode(Key(KeyKind.Meter, 3))
      }
    }
  }
}
