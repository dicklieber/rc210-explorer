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


class MacroNodeSpec extends WithMemory {

  "MacroNode" when {
    "parseChunk" in {
      val chunk: Seq[Int] = Seq(165, 85, 27, 60, 196, 255, 255, 255, 234, 0, 0, 0, 0, 0, 0, 0)
      val iterator = chunk.iterator
      val Keys: Seq[Key] = MacroNode.parseChunk(iterator)
      Keys should have length (6)
      Keys.map(_.rc210Value).mkString(",") should equal("""165,85,27,60,196,999""")
    }

    "parseFunction" in {
      val chunk: Seq[Int] = Seq(165, 85, 27, 60, 196, 255, 255, 255, 234, 0, 0, 0, 0, 0, 0, 0)
      val iterator = chunk.iterator
      MacroNode.parseFunction(iterator).get.rc210Value should equal(165)
      MacroNode.parseFunction(iterator).get.rc210Value should equal(85)
      MacroNode.parseFunction(iterator).get.rc210Value should equal(27)
      MacroNode.parseFunction(iterator).get.rc210Value should equal(60)
      MacroNode.parseFunction(iterator).get.rc210Value should equal(196)
      val i = 255 + 255 + 255 + 234
      i should equal(999)
      MacroNode.parseFunction(iterator).get.rc210Value should equal(i)
      val last: Option[Key] = MacroNode.parseFunction(iterator)
      assert(last.isEmpty)
    }
    "parseFunction empty" in {
      val name = MacroNode.name
      val chunk: Seq[Int] = Seq.fill(16) {
        0
      }
      val iterator = chunk.iterator
      val last = MacroNode.parseFunction(iterator)
      assert(last.isEmpty)
    }
//    "parseFunction no terminator" in {
//      val name = MacroNode.name
//      val chunk: Seq[Int] = Seq.fill(16) {
//        1
//      }
//      val iterator = chunk.iterator
//      var last: Option[Key] = Some(Key(KeyKind.macroKey, 42))
//      var count = 0
//      try {
//        do {
//          last = MacroNode.parseFunction(iterator)
//          count += 1
//        } while (last.nonEmpty)
//      } catch {
//        case e: Exception =>
//          assert(e.isInstanceOf[NoSuchElementException])
//      }
//      count should equal (16)
//    }
  }

  "extract" in {
    val macroNodes = MacroNode.extract(memory)

    macroNodes should have length 90
    val fieldEntry = macroNodes(8)
    val display = fieldEntry.fieldValue.displayHtml
    fieldEntry.fieldKey.key.rc210Value should equal(9)
    display should equal ("165 85 817 818 195")
  }
}
