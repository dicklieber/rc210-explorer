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

import net.wa9nnn.RcSpec
import net.wa9nnn.rc210.key.KeyFactory.{FunctionKey, Key}

class KeyFactorySpec extends RcSpec {
  "Expected Number of Keys" in {
    val nKeys = KeyKind.values().foldLeft(0) { case (accum, kk) =>
      accum + kk.maxN()
    }
    KeyFactory.availableKeys should have length nKeys
    val value1 = KeyFactory(KeyKind.courtesyToneKey)
    value1 should have length (KeyKind.courtesyToneKey.maxN())
  }
  "apply keykind" in {
    val portKeys = KeyFactory(KeyKind.portKey)
    portKeys should have length (3)

    val cts = KeyFactory(KeyKind.courtesyToneKey)
    cts should have length 10
  }

  "apply sKey" in {
    val fKey: FunctionKey = KeyFactory("functionKey1")
    assert(fKey.isInstanceOf[FunctionKey])
    fKey.toString should equal("functionKey1")
  }
  "apply sKey as[Key]" in {
    val fKey: Key = KeyFactory("functionKey1")
    assert(fKey.isInstanceOf[FunctionKey])
    fKey.toString should equal("functionKey1")
  }

  "apply" in {
    val functionKey: FunctionKey = KeyFactory(KeyKind.functionKey, 1)
    functionKey.number should equal(1)
    functionKey.kind should equal(KeyKind.functionKey)
  }

  "unknown key" in {
    an[NoSuchElementException] should be thrownBy KeyFactory[Key]("crap")
  }

  "bad number" in {
    an[NoSuchElementException] should be thrownBy KeyFactory[Key](KeyKind.portKey, 42)
  }

  "kindAndCounts" in {
    val kindAndCounts: Seq[(KeyKind, Int)] = KeyFactory.kindAndCounts
    kindAndCounts should have length (KeyKind.values().length)
    val head = kindAndCounts.head
    head._1 should equal(KeyKind.logicAlarmKey)
    head._2 should equal(5)

    val last = kindAndCounts.last
    last._1 should equal(KeyKind.remoteBaseKey)
    last._2 should equal(1)
  }
}
