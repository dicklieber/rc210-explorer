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
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

import java.awt.BufferCapabilities

/**
 * FieldExtractors know how to parse a part of [[Memory]] and produce a [[FieldContents]]
 *
 * @param bytesPerField how much to slice pff.
 */

abstract class FieldExtractor(bytesPerField: Int) {

  def apply(memory: Memory, offset: Int): ExtractResult = {
    val slicePos = SlicePos(offset, bytesPerField)
    ExtractResult(extract(memory(slicePos)), slicePos.until)
  }

  def extract(slice: Slice): FieldContents

  override def toString: String = name

  val name: String
}

object FieldExtractors {

  val bool: FieldExtractor = new FieldExtractor(1) {
    override def extract(slice: Slice): FieldContents = FieldBoolean(slice, (slice.head > 0))

    override val name: String = "bool"
  }

  val int8: FieldExtractor = new FieldExtractor(1) {
    override def extract(slice: Slice) = FieldInt(slice, slice.head)

    override val name: String = "int8"
  }
  val int16: FieldExtractor = new FieldExtractor(2) {
    override def extract(slice: Slice): FieldInt = {
      val iterator = slice.iterator
      val intValue = iterator.next() + iterator.next() * 256
      FieldInt(slice, intValue)
    }

    override val name: String = "int16"
  }

  val twoInts: FieldExtractor = new FieldExtractor(4) {
    override def extract(slice: Slice): FieldContents = {
      val ints = slice.grouped(2).map { slice =>
        int16.extract(slice)
      }
      FieldSeqInts(slice, ints.map { f => f.asInstanceOf[FieldInt].value }: _*)
    }

    override val name: String = "cwTones"

  }
}

// this works for any number up to 8 digits
case class DtmfExtractor(maxDigits: Int) extends FieldExtractor(maxDigits + 1) with LazyLogging {
  override def extract(slice: Slice): FieldContents = {
    val r = FieldDtmf(slice, new String(slice.data
      .takeWhile(_ != 0)
      .map(_.toChar) //todo how about A-D?
      .toArray
    )
    )
    logger.trace("extract: {} from slice: {}", r, slice)
    r
  }

  override val name: String = "dtmf"
}


case class ExtractResult(contents: FieldContents, newOffset: Int)
