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
import net.wa9nnn.rc210.key.WordKey

import scala.io.BufferedSource
import scala.util.{Failure, Success, Using}

object Vocabulary {
  val phrases: Seq[Phrase] = Using(new BufferedSource(getClass.getResourceAsStream("/Vocabulary.txt"))) { bs: BufferedSource =>
    bs.getLines()
      .map { line: String =>
        val n = line.take(3)
        val t = line.drop(4).trim
        Phrase(WordKey(n.toInt + 1), t)
      }
      .toSeq
      .sortBy(_.string)

  } match {
    case Failure(exception) =>
      throw exception
    case Success(value) =>
      value
  }
  private val byKey: Map[WordKey, Phrase] = {
    phrases.map(w => w.wordKey -> w).toMap
  }
  private val byText: Map[String, Phrase] = phrases.map { w =>
    w.string -> w
  }.toMap


  /**
   * Lookup by string.
   */
  def apply(string: String): Phrase = byText(string)

  /**
   * Lookup by Key
   */
  def apply(wordKey: WordKey): Phrase = byKey(wordKey)
}

/**
 *
 * @param wordKey  id
 * @param string   what shows or would be spoken.
 */
case class Phrase(wordKey: WordKey, string: String) extends RowSource {
  override def toRow: Row = Row(wordKey.toCell, string)
}

object Phrase {
  def header(count:Int):Header = Header(s"Phrases ($count)", "Key", "String")
}