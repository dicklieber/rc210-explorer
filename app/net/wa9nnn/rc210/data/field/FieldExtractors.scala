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
import net.wa9nnn.rc210.command.ItemValue
import net.wa9nnn.rc210.command.ItemValue.Values
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

///**
// */
//object FieldExtractors extends LazyLogging {
//  def apply(exractorName: String, slice: Slice): String = {
//    exractorName match {
//      case "dtmf" =>
//        new String(slice.data
//          .takeWhile(_ != 0)
//          .map(_.toChar) //todo how about A-D?
//          .toArray
//        )
//      case "bool" =>
//        (slice.head > 0).toString
//      case "int8" =>
//        slice.head.toString
//      case "int16" =>
//        slice.head.toString
//
//      // more go here
//      case x =>
//        throw new IllegalArgumentException("Don't have a fieldExtractor named: x!")
//
//    }
//  }
//}

abstract class FieldExtractor(bytesPreField: Int) {

  def apply(memory: Memory, offset: Int): ExtractResult = {
    val slicePos = SlicePos(offset, bytesPreField)
    ExtractResult(extract(memory(slicePos)), slicePos.until)
  }

  def extract(slice: Slice): String

  override def toString: String = name

  val name:String
}

object FieldExtractors {

  // this works for any number up to 8 digits
  // Note the ExtractRessult wont be right
  val dtmf: FieldExtractor = new FieldExtractor(9) {
    override def extract(slice: Slice) =
      new String(slice.data
        .takeWhile(_ != 0)
        .map(_.toChar) //todo how about A-D?
        .toArray
      )

    override val name: String = "dtmf"
  }
  val bool: FieldExtractor = new FieldExtractor(1) {
    override def extract(slice: Slice): String = (slice.head > 0).toString
    override val name: String = "bool"
  }

  val int8: FieldExtractor = new FieldExtractor(1) {
    override def extract(slice: Slice) = slice.head.toString
    override val name: String = "int8"

  }
  val int16: FieldExtractor = new FieldExtractor(2) {
    override def extract(slice: Slice) = {
      val iterator = slice.iterator
      val intValue = iterator.next() + iterator.next() * 256
      intValue.toString
    }
    override val name: String = "int16"

  }

  val twoInts: FieldExtractor = new FieldExtractor(4) {
    override def extract(slice: Slice) = {
      val grouped = slice.grouped(2)
      grouped.map { slice =>
        int16.extract(slice)
      }
        .mkString(" ")
    }
    override val name: String = "cwTones"

  }
  val unlock: FieldExtractor = new FieldExtractor(9) {
    override def extract(slice: Slice): String = {
      slice
        .data
        .grouped(9).zipWithIndex
        .map { case (v, port) =>
          val value: Array[Char] = v.takeWhile(_ != 0).map(_.toChar).toArray
          new String(value)
        }
        .toSeq
        .mkString(" ")
    }
    override val name: String = "unlock"

  }

}

case class ExtractResult(value: String, newOffset: Int)
