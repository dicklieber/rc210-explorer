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

import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.key.KeyFactory.Key
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAll

class NamedKeySpec extends Specification with BeforeAll with Mockito {
  private val portKey1 = KeyFactory.portKey(1)
  private val portKey2 = KeyFactory.portKey(2)

  override def beforeAll(): Unit = {
    val namedSource = mock[NamedSource]

    Key.setNamedSource(namedSource)
    namedSource.nameForKey(portKey1).returns("Harpo")
    namedSource.nameForKey(portKey2).returns("")

  }

  "NamedKey" >> {
    "Name set to Harpo" >> {
      val cell = portKey1.namedCell()
      cell.value must beEqualTo(
        """
          |<div class="keyName">
          |    <label for="name:portKey1">1: </label>
          |    <input name="name:portKey1" id="name:portKey1" value="Harpo" title="User-supplied name.">
          |</div>""".stripMargin)
    }
    "No name set" >> {
      val cell = portKey2.namedCell()
      cell.value must beEqualTo(
        """
          |<div class="keyName">
          |    <label for="name:portKey2">2: </label>
          |    <input name="name:portKey2" id="name:portKey2" value="" title="User-supplied name.">
          |</div>""".stripMargin)
    }

    "Custom parameter" >> {
      val cell = portKey2.namedCell("custom")
      cell.value must beEqualTo(
        """
          |<div class="keyName">
          |    <label for="custom">2: </label>
          |    <input name="custom" id="custom" value="" title="User-supplied name.">
          |</div>""".stripMargin)

    }
  }
}
