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
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry}
import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.data.vocabulary.Words
import net.wa9nnn.rc210.key.KeyFactory.{MessageKey, logicAlarmKey}
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
    /*    <div class="container">
          <table class="table table-bordered w-auto">
            <thead class="table-primary">
              <tr>
                <th></th>
                <th>Message</th>
                <th>Words</th>
              </tr>
            </thead>
            <tbody>
              @for(message
              <- messages)
                {<tr>
                <td>
                  <button type="button" class="bi bi-pencil-square btn p-0"
                          onclick="window.location.href = '@routes.MessageController.edit(message.key)'"></button>
                </td>
                <td title="@message.key.toString">
                  @namedSource.nameForKey(message.key)
                </td>
                <td>
                  @message.display
                </td>

              </tr>}
              </tbody>
            </table>
          </div>
    */

    val rowHeader = key.namedCell()
    Row(rowHeader, words.map { wordKey =>
      Cell(Words.apply(wordKey).string)
        .withToolTip(wordKey.toString)
    }
    )
  }

  override def display: String = words.map(wordKey => Words(wordKey).string).mkString(" ")

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = "//todo"

  override def toJsonValue: JsValue = Json.toJson(this)
}

object Message extends LazyLogging {

  import net.wa9nnn.rc210.key.KeyFormats._

  def header(count: Int): Header = Header(s"Messages Macros ($count)", "Key", "Words")

  implicit val fmtPhrase: Format[Message] = Json.format[Message]

  //  def apply(values:Map[String,String]):Message = {
  //    logger.info(s"todo $values")
  //    val sKey = formData("key").head
  //    val key: MessageKey = KeyFactory(sKey)
  //
  //    val strings: Array[String] = formData("words").head.split(",").filter(_.nonEmpty)
  //
  //    val words: Seq[Int] = strings.map { s =>
  //      s.toInt
  //    }.toIndexedSeq
  //
  //    val message = Message(key, words)
  //
  //  }
}

