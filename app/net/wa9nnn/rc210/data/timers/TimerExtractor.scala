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
import net.wa9nnn.rc210.MemoryExtractor
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldInt}
import net.wa9nnn.rc210.key.KeyFactory.TimerKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import net.wa9nnn.rc210.util.MacroSelect

//noinspection ZeroIndexToHead
object TimerExtractor extends MemoryExtractor with LazyLogging {
  private val nTimers = KeyKind.timerKey.maxN()
  //  Memory Layout
  //  seconds for each timer 6 2-byte ints
  //  macroToRun for each timer 6 2-byte ints


  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val secondsData: Seq[Int] = memory.apply(SlicePos(1553, nTimers * 2))
      .data
      .grouped(2)
      .map { twoBytes: Seq[Int] =>
        twoBytes(0) + (twoBytes(1) * 256)
      }.toSeq
    val macroData: Seq[MacroSelect] = memory.apply(SlicePos(1565, nTimers))
      .data
      .map { number =>
        MacroSelect(number)
      }.toSeq

   val r =  (for {
      index <- 0 until KeyKind.timerKey.maxN()
    } yield {
      val key: TimerKey = KeyFactory(KeyKind.timerKey, index + 1)
      FieldEntry(this,
        FieldKey("Timer", key),

        Timer(key, FieldInt(secondsData(index)), macroData(index)))
    })
    r
  }

  override val fieldName: String = "Courtesy Tone"
  override val kind: KeyKind = KeyKind.courtesyToneKey
}
