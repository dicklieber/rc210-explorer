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
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import net.wa9nnn.rc210.data.courtesy.CourtesyTonesExtractor.logger
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue, FieldEntry, FieldOffset, FieldValue}
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.{Format, JsValue, Json}

/**
 * [[CourtesyTone]] data are spreead out in the [[Memory]] image.
 * This gaters all the data and produces all the [[CourtesyTone]]s.
 */
object CourtesyTonesExtractor extends ComplexExtractor with LazyLogging {
  override val keyKind: KeyKind = KeyKind.CourtesyTone
  private val nCourtesyTones = keyKind.maxN

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(856, this),
  )

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    logger.debug("Extract CourtesyTone")
    val iterator = memory.iterator16At(856)

    val array = Array.ofDim[Int](10, 16)
    for {
      part <- 0 until 16
      ct <- 0 until nCourtesyTones
    } {
      array(ct)(part) = iterator.next()
    }

    val courtesyTones: Seq[CourtesyTone] = for (ct <- 0 until 10) yield {
      val key: Key = Key(KeyKind.CourtesyTone, ct + 1)
      val segments = Seq(
        Segment(array(ct)(8), array(ct)(12), array(ct)(0), array(ct)(1)),
        Segment(array(ct)(9), array(ct)(13), array(ct)(2), array(ct)(3)),
        Segment(array(ct)(10), array(ct)(14), array(ct)(4), array(ct)(5)),
        Segment(array(ct)(11), array(ct)(15), array(ct)(6), array(ct)(7)),
      )
      CourtesyTone(key, segments)
    }
    courtesyTones.map { ct =>
      FieldEntry(this, FieldKey(fieldName, ct.key), ct)
    }
  }


  override def parse(jsValue: JsValue): FieldValue = jsValue.as[CourtesyTone]


  override val name: String = "CourtesyExtractor"
  //  override val fieldName: String = name

  implicit val fmtCourtesyTone: Format[CourtesyTone] = Json.format[CourtesyTone]
  override val fieldName: String = "CourtesyTone"
}