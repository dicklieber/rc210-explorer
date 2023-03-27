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

import net.wa9nnn.rc210.key.KeyFactory.{FunctionKey, Key}
import org.specs2.mutable.Specification

class KeyFactorySpec extends Specification {

  "KeyFactorySpec" should {
    "apply keykind" in {
      val portKeys = KeyFactory(KeyKind.portKey)
      portKeys must haveLength(3)
    }

    "apply sKey" in {
      val fKey: FunctionKey = KeyFactory("functionKey1")
      fKey must beAnInstanceOf[FunctionKey]
      fKey.toString must beEqualTo("functionKey1")
    }
    "apply sKey as[Key]" in {
      val fKey: Key = KeyFactory("functionKey1")
      fKey must beAnInstanceOf[FunctionKey]
      fKey.toString must beEqualTo("functionKey1")
    }

    "apply" in {
      val functionKey: FunctionKey = KeyFactory(KeyKind.functionKey, 1)
      functionKey.number must beEqualTo(1)
      functionKey.kind must beEqualTo(KeyKind.functionKey)
    }

    "unknown key" in {
      KeyFactory[Key]("crap") must throwAn[IllegalArgumentException]
    }

    "bad number" in {
      KeyFactory[Key](KeyKind.portKey, 42) must throwAn[IllegalArgumentException]
    }

    "availableKeys" in {
      val availableKeys = KeyFactory.availableKeys
      availableKeys must haveLength(1690)
    }
  }
}
