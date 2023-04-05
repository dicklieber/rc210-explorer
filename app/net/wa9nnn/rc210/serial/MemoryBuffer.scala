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

package net.wa9nnn.rc210.serial

import scala.collection.immutable.ArraySeq

/**
 * Access RC-210 memory with iterators over 8 (1 bute) or 16 (2 byte) integers.
 *
 * @param data mutable array.
 */
class MemoryBuffer(data: Array[Int]) {
  private val array = new ArraySeq.ofInt(data)

  def iterator8At(offset: Int): Iterator[Int] = {
    val value: Iterator[Int] = array.drop(offset).iterator
    value
  }

  def iterator16At(offset: Int): Iterator[Int] =
    new Iterator16(offset)

  private class Iterator16(offset: Int) extends Iterator[Int] {
    private val
    iterator8: Iterator[ArraySeq[Int]] = array.drop(offset).sliding(2, 2)

    override def hasNext: Boolean = iterator8.hasNext

    override def next(): Int = {
      val ints: Seq[Int] = iterator8.next()
      ints.head + (ints(1) * 256)
    }
  }
}

