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

package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table}
import controllers.ExtractField
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.LogicAlarmNode.{form, keyMetadata}
import net.wa9nnn.rc210.data.vocabulary.{Word, Words}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.{ButtonCell, FormData}
import net.wa9nnn.rc210.util.Chunk
import net.wa9nnn.rc210.{Key, KeyMetadata}
import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import play.api.data.Form
import play.api.data.Forms.*
import play.api.http.Status
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.{RequestHeader, Result, Results}
import play.twirl.api.Html
import views.html.{fieldIndex, logicAlarmEditor, messageEditor}

import java.util.concurrent.atomic.AtomicInteger
import scala.util.Try

/**
 * These are called "Message Macros in the RC-210 docs, called "Phrases" in the PHP
 * MessageMacro is a bit long, so we use "Message" in the application.
 *
 * @param key   Message key
 * @param words word numbers. Each 0 to 255. 
 */
case class MessageNode(words: Seq[Int]) extends FieldValueComplex[MessageNode]() {

  def toWords: Seq[Word] = words.map(Word(_))

  override def displayCell: Cell = Cell(words.map(id => Word(id).string).mkString(", "))

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntry): Seq[String] = {
    val key = fieldEntry.key
    val value: Seq[String] = words.map(word => f"$word%03d")
    val word3s: String = value.mkString
    Seq(f"1*2103${key.rc210Number.get}%02d$word3s")
  }

  override def toRow(key: Key): Row = {
    Row(
      ButtonCell.editFlow(key),
      key.keyWithName,
      toWords.map(_.string).mkString(" ")
    )
  }
}

object MessageNode extends FieldDefComplex[MessageNode] with LazyLogging:
  implicit val fmt: Format[MessageNode] = Json.format[MessageNode]

  def apply(key: Key, kv: Map[String, String]): MessageNode = {

    val csv: String = kv("words")
    val wordIds: Seq[Int] = csv
      .split(",")
      .toIndexedSeq
      .filter(_.nonEmpty).map(_.toInt)
    new MessageNode(wordIds)
  }

  def form: Form[MessageNode] = throw new IllegalStateException("Not used with MessageNode!")

  override val keyMetadata: KeyMetadata = KeyMetadata.Message

  override def positions: Seq[FieldOffset] = {
    Seq(
      FieldOffset(1576, this)
    )
  }

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    // the PHP code only gets the 40 from main. //Phrase - 1576-1975
    // Have to dig for what cpomes from the RTC board.
    val mai = new AtomicInteger(1)
    for {
      chunk: Chunk <- memory.chunks(1576, 10, 40)
      key: Key = Key(KeyMetadata.Message, mai.getAndIncrement())
    } yield {
      val message: MessageNode = MessageNode(chunk.ints
        .takeWhile(_ != 0))
      FieldEntry(this, key, message)
    }
  }

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val header = Header(s"Messages  (${fieldEntries.length})",
      "",
      "Name",
      "Words"
    )
    val table = Table(header,
      fieldEntries.map(fe =>
      {
        val value:MessageNode = fe.value
        value.toRow(fe.key)
      })
    )
    fieldIndex(keyMetadata, table)

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    messageEditor(fieldEntry.key, fieldEntry.value)

  override def bind(formData: FormData): Seq[UpdateCandidate] =
    (for {
      fieldKey <- Option(formData.key)
      ids <- formData("ids")
    } yield {
      val strings: Seq[String] = ids.split(',').toIndexedSeq
      val messageNode = MessageNode(strings.map(_.toInt))
      UpdateCandidate(fieldKey, messageNode)
    }).toSeq




