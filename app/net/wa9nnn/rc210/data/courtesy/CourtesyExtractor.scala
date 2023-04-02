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

package net.wa9nnn.rc210.data.courtesy

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.MemoryExtractor
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.key.KeyFactory.CourtesyToneKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

object CourtesyExtractor extends MemoryExtractor with LazyLogging {
  val nCourtesyTones = KeyKind.courtesyToneKey.maxN()
  val nSegmentsPerCt = 4
  val intsPerSegment = 4

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val slicePos = SlicePos(856, nCourtesyTones * nSegmentsPerCt * intsPerSegment * 2)
    val toneSlice: Slice = memory.apply(slicePos)
    val data: Iterator[Seq[Int]] = toneSlice.data.grouped(2)
    val int16s: Seq[Int] = data.map((ints: Seq[Int]) => ints.head + (ints(1) * 256)).toSeq


    val array = Array.ofDim[Int](10, 16)
    val iterator = int16s.iterator
    for {
      part <- 0 until (16)
      ct <- 0 until (nCourtesyTones)
    } {
      array(ct)(part) = iterator.next()
    }

    val courtesytones: Seq[CourtesyTone] = for (ct <- 0 until (10)) yield {
      val key:CourtesyToneKey = KeyFactory(KeyKind.courtesyToneKey, ct + 1)
      CourtesyTone(key,
        Seq(
          Segment(array(ct)(8), array(ct)(12), array(ct)(0), array(ct)(1)),
          Segment(array(ct)(9), array(ct)(13), array(ct)(2), array(ct)(3)),
          Segment(array(ct)(10), array(ct)(14), array(ct)(4), array(ct)(5)),
          Segment(array(ct)(11), array(ct)(15), array(ct)(6), array(ct)(7)),
        ))
    }
  courtesytones.map { ct =>
      FieldEntry(this, FieldKey("CourtesyTone", ct.key), ct)
    }
  }

  override val fieldName: String = "Courtesy Tone"
  override val kind: KeyKind = KeyKind.courtesyToneKey
}
