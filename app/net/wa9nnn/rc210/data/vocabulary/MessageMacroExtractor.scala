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

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import net.wa9nnn.rc210.{MessageMacroKey, WordKey}
import play.api.libs.json.{Json, OFormat}

case class MessageMacro(key: MessageMacroKey, words: Seq[WordKey]) extends RowSource{
  override def toRow: Row = {
    Row(key.toCell, words.map(Vocabulary(_)).mkString(" "))
  }
}

object MessageMacro {
  def header(count: Int):Header = Header(s"Message Macros ($count)", "Key", "Words")

  implicit val fmtMessageMacro: OFormat[MessageMacro] = Json.format[MessageMacro]
}


/**
 * The PHP code calls MessageMacros Phrases; which is probably a better term than MessageMacro but
 * MessageMacro is what's in thre RC-210 programming manual.
 */
object MessageMacroExtractor {

  def apply(memory: Memory): Seq[MessageMacro] = {
    val slice = memory(SlicePos("//Phrase - 1576-1975"))
    slice.data
      .grouped(10)
      .zipWithIndex
      .map { case (words, k) =>
        val w = words.takeWhile(_ != 0)
        MessageMacro(MessageMacroKey(k), w.map(WordKey))
      }
      .toSeq
  }

}
