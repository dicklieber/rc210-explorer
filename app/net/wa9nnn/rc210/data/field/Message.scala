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
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import net.wa9nnn.rc210.data.vocabulary.Word
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.util.Chunk
import play.api.data.Form
import play.api.libs.json.{Format, JsValue, Json}

import java.util.concurrent.atomic.AtomicInteger

/**
 * These are called "Message Macros in the RC-210 docs, called "Phrases" in the PHP
 * MessageMacro is a bit long, so we use "Message" in the application.
 *
 * @param key   Message key
 * @param words word numbers. Each 0 to 255. 
 */
case class Message(key: Key, words: Seq[Int]) extends ComplexFieldValue() {

  def toWords: Seq[Word] = words.map(Word(_))

  override def displayHtml: String = words.map(id => Word(id).string).mkString(", ")

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val value: Seq[String] = words.map(word => f"$word%03d")
    val word3s: String = value.mkString
    Seq(f"1*2103${key.rc210Value}%02d$word3s")
  }

  override def toJsValue: JsValue = Json.toJson(this)
}
object Message extends ComplexExtractor[Message] with LazyLogging {
  implicit val fmtPhrase: Format[Message] = Json.format[Message]

  def apply(key: Key, kv: Map[String, String]): Message = {

    val csv: String = kv("words")
    val wordIds: Seq[Int] = csv
      .split(",")
      .toIndexedSeq
      .filter(_.nonEmpty).map(_.toInt)
    new Message(key, wordIds)
  }

  override val keyKind: KeyKind = KeyKind.Message

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
      key: Key = Key(KeyKind.Message, mai.getAndIncrement())
    } yield {
      val message: Message = Message(key, chunk.ints
        .takeWhile(_ != 0)
      )
      val fieldKey = FieldKey(fieldName, key)
      FieldEntry(this, fieldKey, message)
    }
  }

  /**
   * for various things e.g. parser name.
   */
  override val name: String = "Message"

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[Message]

  override val fieldName: String = name

  override val form: Form[Message] = throw new NotImplementedError("No fprm used with Message!") //todo
}
