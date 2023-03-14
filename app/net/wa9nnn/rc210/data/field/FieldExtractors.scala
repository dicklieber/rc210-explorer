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
import net.wa9nnn.rc210.data.field.FieldExtractors.int8
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

/**
 * FieldExtractors know how to parse a [[Slice]] of [[Memory]] and produce a [[FieldContents]]
 *
 * @param bytesPerField how much to slice pff for this field.
 */

abstract class SimpleFieldExtractor(val bytesPerField: Int) {

  def apply(memory: Memory, offset: Int): (FieldContents, Slice) = {
    val slice = memory(SlicePos(offset, bytesPerField))
    extract(slice) -> slice
  }

  /**
   * in sub class.
   * @param slice within [[Memory]]
   * @return
   */
  def extract(slice: Slice): FieldContents

  override def toString: String = name

  val name: String
}

object FieldExtractors {

  val bool: SimpleFieldExtractor = new SimpleFieldExtractor(1) {
    override def extract(slice: Slice): FieldContents = FieldBoolean(slice.head > 0)

    override val name: String = "bool"
  }

  val int8: SimpleFieldExtractor = new SimpleFieldExtractor(1) {
    override def extract(slice: Slice) = FieldInt( slice.head)

    override val name: String = "int8"
  }
  val int16: SimpleFieldExtractor = new SimpleFieldExtractor(2) {
    override def extract(slice: Slice): FieldInt = {
      val iterator = slice.iterator
      val intValue = iterator.next() + iterator.next() * 256
      FieldInt( intValue)
    }

    override val name: String = "int16"
  }

  val twoInts: SimpleFieldExtractor = new SimpleFieldExtractor(4) {
    override def extract(slice: Slice): FieldContents = {
      val ints = slice.grouped(2).map { slice =>
        int16.extract(slice)
      }
      FieldSeqInts( ints.map { f => f.asInstanceOf[FieldInt].value }: _*)
    }

    override val name: String = "cwTones"

  }
}

// this works for any number up to 8 digits
case class DtmfExtractor(maxDigits: Int) extends SimpleFieldExtractor(maxDigits + 1) with LazyLogging {
  override def extract(slice: Slice): FieldContents = {
    FieldDtmf( new String(slice.data
      .takeWhile(_ != 0)
      .map(_.toChar) //todo how about A-D?
      .toArray
    ))
  }


  override val name: String = "dtmf"
}

// Select field
case class SelectExtractor() extends SimpleFieldExtractor(1) with LazyLogging {
  override def extract(slice: Slice): FieldContents = {

    val contents: Int = int8.extract(slice).asInstanceOf[FieldInt].value
    val r = FieldSelect( contents)
    logger.trace("extract: {} from slice: {}", r, slice)
    r
  }

  override val name: String = "dtmf"
}

