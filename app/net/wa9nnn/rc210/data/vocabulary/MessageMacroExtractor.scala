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

package net.wa9nnn.rc210.data.vocabulary

import com.wa9nnn.util.tableui._
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.model.Node
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import net.wa9nnn.rc210.{MemoryExtractor, MessageMacroKey, WordKey}

case class MessageMacroNode(key: MessageMacroKey, words: Seq[WordKey]) extends RowSource with Node with CellProvider {
  override def toRow: Row = {
    Row(key.toCell, toCell)
  }

  override def toCell: Cell =
    Cell(words.map { wordKey =>
      val phrase: Phrase = Vocabulary(wordKey)
      phrase.string
    }.mkString(" "))
}

object MessageMacroNode {
  def header(count: Int): Header = Header(s"Message Macros ($count)", "Key", "Words")

}


/**
 * The PHP code calls MessageMacros Phrases; which is probably a better term than MessageMacro but
 * MessageMacro is what's in thre RC-210 programming manual.
 */
class MessageMacroExtractor extends MemoryExtractor {
  /**
   *
   * @param memory    source of RC-210 data.
   * @param rc210Data internal to have our data appended to it.
   * @return the inputted rc210Data with our data inserted into it.
   */
  override def apply(memory: Memory, rc210Data: Rc210Data): Rc210Data = {
    val slice = memory(SlicePos("//Phrase - 1576-1975"))
    val r = slice.data
      .grouped(10)
      .zipWithIndex
      .map { case (words, k) =>
        val w = words.takeWhile(_ != 0)
        MessageMacroNode(MessageMacroKey(k), w.map(WordKey))
      }
      .toSeq

    rc210Data.copy(messageMacros = r)
  }

}
