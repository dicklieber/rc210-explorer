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
  "Expected Number of Keys" >> {
    val nKeys = KeyKind.values().foldLeft(0) { case (accum, kk) =>
      accum + kk.maxN()
    }
    KeyFactory.availableKeys must haveLength(nKeys)
    val value1 = KeyFactory(KeyKind.courtesyToneKey)
    value1 must haveLength(KeyKind.courtesyToneKey.maxN())
  }
  "apply keykind" in {
    val portKeys = KeyFactory(KeyKind.portKey)
    portKeys must haveLength(3)

    val cts = KeyFactory(KeyKind.courtesyToneKey)
    cts must haveLength(10)
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

  "kindAndCounts" >> {
    val kindAndCounts: Seq[(KeyKind, Int)] = KeyFactory.kindAndCounts
    kindAndCounts must haveLength(KeyKind.values().length)
    val head = kindAndCounts.head
    head._1 must beEqualTo(KeyKind.logicAlarmKey)
    head._2 must beEqualTo(5)

    val last = kindAndCounts.last
    last._1 must beEqualTo(KeyKind.scheduleKey)
    last._2 must beEqualTo(40)
  }
}
