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

import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

import java.awt.BufferCapabilities

/**
 * FieldExtractors know how to parse a part of [[Memory]] and produce a [[FieldContents]]
 *
 * @param bytesPreField how much to slice pff.
 */

abstract class FieldExtractor(bytesPreField: Int) {

  def apply(memory: Memory, offset: Int): ExtractResult = {
    val slicePos = SlicePos(offset, bytesPreField)
    ExtractResult(extract(memory(slicePos)), slicePos.until)
  }

  def extract(slice: Slice): FieldContents

  override def toString: String = name

  val name: String
}

object FieldExtractors {

  // this works for any number up to 8 digits
  // Note the ExtractResult won't be right
  val dtmf: FieldExtractor = new FieldExtractor(9) {
    override def extract(slice: Slice): FieldContents =
      FieldDtmf(new String(slice.data
        .takeWhile(_ != 0)
        .map(_.toChar) //todo how about A-D?
        .toArray
      )
      )

    override val name: String = "dtmf"
  }
  val bool: FieldExtractor = new FieldExtractor(1) {
    override def extract(slice: Slice): FieldContents = FieldBoolean((slice.head > 0))

    override val name: String = "bool"
  }

  val int8: FieldExtractor = new FieldExtractor(1) {
    override def extract(slice: Slice) = FieldInt(slice.head)

    override val name: String = "int8"
  }
  val int16: FieldExtractor = new FieldExtractor(2) {
    override def extract(slice: Slice): FieldInt = {
      val iterator = slice.iterator
      val intValue = iterator.next() + iterator.next() * 256
      FieldInt(intValue)
    }

    override val name: String = "int16"
  }

  val twoInts: FieldExtractor = new FieldExtractor(4) {
    override def extract(slice: Slice): FieldContents = {
      val ints = slice.grouped(2).map { slice =>
        int16.extract(slice)
      }
      FieldSeqInts(ints.map { f => f.asInstanceOf[FieldInt].value }:_*)
    }

    override val name: String = "cwTones"

  }
  val unlock: FieldExtractor = new FieldExtractor(9) {
    override def extract(slice: Slice): FieldContents = {
      FieldDtmf(slice
        .data
        .grouped(9).zipWithIndex
        .map { case (v, port) =>
          val value: Array[Char] = v.takeWhile(_ != 0).map(_.toChar).toArray
          new String(value)
        }
        .toSeq
        .mkString(" "))
    }

    override val name: String = "unlock"

  }

}

case class ExtractResult(contents: FieldContents, newOffset: Int)
