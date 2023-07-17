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

import com.wa9nnn.util.tableui.{Cell, Header, Row, RowSource}
import play.api.libs.json.{Format, JsValue, Json}

import scala.io.BufferedSource
import scala.util.{Failure, Success, Using}

object Words {
  val words: Seq[Word] = Using(new BufferedSource(getClass.getResourceAsStream("/Vocabulary.txt"))) { bs: BufferedSource =>
    bs.getLines()
      .map { line: String =>
        val n: String = line.take(3)
        val t = line.drop(4).trim
        Word(n.toInt, t)
      }
      .toSeq

  } match {
    case Failure(exception) =>
      throw exception
    case Success(value) =>
      value
  }
  private val byText: Map[String, Word] = words.map { w =>
    w.string -> w
  }.toMap


  /**
   * Lookup by string.
   */
  def apply(string: String): Word = byText(string)

}

/**
 * A things the the RC-210 can say.
 *
 * @param id  id.
 * @param string   what shows or would be spoken.
 */
case class Word(id: Int, string: String) extends RowSource {
  override def toRow: Row = Row(Cell(id), string)
  val json: JsValue = Json.toJson(this)
}

object Word {
  implicit val fmtWord: Format[Word] = Json.format[Word]
  def header(count: Int): Header = Header(s"Phrases ($count)", "Key", "String")

  val words: Seq[Word] = Using(new BufferedSource(getClass.getResourceAsStream("/Vocabulary.txt"))) { bs: BufferedSource =>
    bs.getLines()
      .map { line: String =>
        val n = line.take(3)
        val t = line.drop(4).trim
        Word(n.toInt, t)
      }
      .toSeq
      .sortBy(_.string)

  } match {
    case Failure(exception) =>
      throw exception
    case Success(value) =>
      value
  }
  private val byText: Map[String, Word] = words.map { w =>
    w.string -> w
  }.toMap
  private val byId: Map[Int, Word] = words.map { w =>
    w.id -> w
  }.toMap


  /**
   * Lookup by string.
   */
  def apply(string: String): Word = byText(string)

  /**
   * Lookup by Key
   */
  def apply(id: Int): Word = byId(id)

  def parse(sJson:String): Word = Json.parse(sJson).as[Word]
}