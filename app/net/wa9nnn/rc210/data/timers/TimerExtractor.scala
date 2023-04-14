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

package net.wa9nnn.rc210.data.timers

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{ComplexExtractor, FieldEntry, FieldInt, FieldValue}
import net.wa9nnn.rc210.key.KeyFactory.TimerKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.util.MacroSelect
import play.api.libs.json.{Format, JsValue, Json}

//noinspection ZeroIndexToHead
object TimerExtractor extends ComplexExtractor with LazyLogging {
  private val nTimers = KeyKind.timerKey.maxN()
  //  Memory Layout
  //  seconds for each timer 6 2-byte ints
  //  macroToRun for each timer 6 1-byte ints


  /**
   *
   * @param memoryBuffer    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memoryBuffer: Memory): Seq[FieldEntry] = {
    val seconds = memoryBuffer.iterator16At(1553)
    val macros = memoryBuffer.iterator8At(1565)


    val r: Seq[FieldEntry] = (for {
      index <- 0 until KeyKind.timerKey.maxN()
    } yield {
      val key: TimerKey = KeyFactory(KeyKind.timerKey, index + 1)
      val fieldKey = FieldKey("Timer", key)
      val macroSelect: MacroSelect = MacroSelect(macros.next())
      FieldEntry(this, fieldKey, Timer(key, FieldInt(seconds.next()), macroSelect))
    })
    r
  }
  import net.wa9nnn.rc210.key.KeyFormats._
  implicit val fmtTimer: Format[Timer] = Json.format[Timer]

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[Timer]

  override val name: String = "Timer"
  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.timerKey
}
