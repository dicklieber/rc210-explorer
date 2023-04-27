package net.wa9nnn.rc210.data.macros

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


import net.wa9nnn.rc210.fixtures.WithMemory
import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.key.KeyFactory.{FunctionKey, functionKey}
import org.specs2.mutable.Specification

class MacroNodeSpec extends WithMemory {

  "MacroNode" should {
    "parseChunk" in {
      val chunk: Seq[Int] = Seq(165, 85, 27, 60, 196, 255, 255, 255, 234, 0, 0, 0, 0, 0, 0, 0)
      val iterator = chunk.iterator
      val functionKeys: Seq[FunctionKey] = MacroNode.parseChunk(iterator)
      functionKeys must haveLength(6)
      functionKeys.map(_.number).mkString(",") must beEqualTo ("""165,85,27,60,196,999""")
    }

    "parseFunction" in {
      val chunk: Seq[Int] = Seq(165, 85, 27, 60, 196, 255, 255, 255, 234, 0, 0, 0, 0, 0, 0, 0)
      val iterator = chunk.iterator
      MacroNode.parseFunction(iterator).get.number must beEqualTo(165)
      MacroNode.parseFunction(iterator).get.number must beEqualTo(85)
      MacroNode.parseFunction(iterator).get.number must beEqualTo(27)
      MacroNode.parseFunction(iterator).get.number must beEqualTo(60)
      MacroNode.parseFunction(iterator).get.number must beEqualTo(196)
      val i = 255 + 255 + 255 + 234
      i must beEqualTo(999)
      MacroNode.parseFunction(iterator).get.number must beEqualTo(i)
      val last = MacroNode.parseFunction(iterator)
      last must beNone
    }
    "parseFunction empty" in {
      val name = MacroNode.name
      val chunk: Seq[Int] = Seq.fill(16) {
        0
      }
      val iterator = chunk.iterator
      val last = MacroNode.parseFunction(iterator)
      last must beNone
    }
    "parseFunction no terminator" in {
      val name = MacroNode.name
      val chunk: Seq[Int] = Seq.fill(16) {
        1
      }
      val iterator = chunk.iterator
      var last: Option[FunctionKey] = Some(KeyFactory.functionKey(42))
      var count = 0
      try {
        do {
          last = MacroNode.parseFunction(iterator)
          count += 1
        } while (last.nonEmpty)
      } catch {
        case e:Exception =>
          e must beAnInstanceOf[NoSuchElementException]
      }
      count must beEqualTo (16)
    }
  }

  "extract" >> {
    val macroNodes = MacroNode.extract(memory)

    macroNodes must haveLength(90)
    val fieldEntry = macroNodes(8)
    val display = fieldEntry.fieldValue.display
    fieldEntry.fieldKey.key.number must beEqualTo (9)
    display must beEqualTo ("165 85 817 818 195")
  }
}
