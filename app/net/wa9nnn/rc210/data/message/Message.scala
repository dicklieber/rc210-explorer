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

package net.wa9nnn.rc210.data.message

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row}
import controllers.routes
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry}
import net.wa9nnn.rc210.data.vocabulary.Words
import net.wa9nnn.rc210.key.KeyFactory.{Key, MessageKey}
import net.wa9nnn.rc210.ui.EditButton
import play.api.libs.json.{Format, JsValue, Json}

import scala.collection.immutable.Seq


/**
 * These are called "Message Macros in the RC-210 docs, callled "Phases" in the PHP
 * MessageMacro is a bit long, so we use "Message" in the application.
 *
 * @param key   Message key
 * @param words word numbers. Each 0 to 255. These are not [[net.wa9nnn.rc210.key.KeyFactory.Key]]s as they are 0 to 255 instead of 1 to N.
 */
case class Message(key: MessageKey, words: Seq[Int]) extends ComplexFieldValue[MessageKey] {

  override val fieldName: String = "Message"

  override def toRow: Row = {

    val editButton = EditButton(routes.MessageController.edit(key))
    val rowHeader = key.toCell
    val csvWords: Cell = Cell(display )
    Row(editButton, rowHeader, csvWords)
  }

  override def display: String = words.map(id => Words(id).string).mkString(", ")

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = "//todo"

  override def toJsonValue: JsValue = Json.toJson(this)
}

object Message extends LazyLogging {

  import net.wa9nnn.rc210.key.KeyFormats._

  def header(count: Int): Header = Header(s"Messages Macros ($count)", "Edit", "Key", "Words")

  implicit val fmtPhrase: Format[Message] = Json.format[Message]

  def apply(key: Key, kv: Map[String, String]): Message = {

    val words: Array[Int] = kv("words").split(",").filter(_.nonEmpty).map(_.toInt)
    new Message(key.asInstanceOf[MessageKey], words)
  }
}

