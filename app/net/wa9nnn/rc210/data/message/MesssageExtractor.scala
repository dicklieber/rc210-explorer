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
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue, FieldEntry, FieldKey, FieldOffset, FieldValue}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.FormFields
import net.wa9nnn.rc210.util.Chunk
import play.api.libs.json.JsValue

import java.util.concurrent.atomic.AtomicInteger

object MesssageExtractor extends ComplexExtractor with LazyLogging {
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
      key: Key = Key(KeyKind.messageKey, mai.getAndIncrement())
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
  override val kind: KeyKind = KeyKind.messageKey

  override def parseForm(formFields: FormFields): ComplexFieldValue = ???
}