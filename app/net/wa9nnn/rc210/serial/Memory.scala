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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.util.Chunk

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, Path}
import java.time.Instant
import scala.collection.immutable.ArraySeq
import scala.io.BufferedSource
import scala.util.matching.Regex
import scala.util.{Try, Using}

/**
 * Contains RC-210 memory with iterators over 8 (1 byte) or 16 (2 byte) integers.
 * and a few more helpers.
 *
 * @param data mutable array. The 1st 4097 ints are main memory the last
 */
class Memory(val data: Array[Int] = Array.empty) {
  def bool(offset: Int): Boolean = data(offset) == 1

  def apply(offset: Int): Int = data(offset)

  val length: Int = data.length

  private val array = new ArraySeq.ofInt(data)

  def stringAt(offset: Int):String = {
    new String(iterator8At(offset)
      .takeWhile(_ != 0)
      .map(_.toChar)
      .toArray)
  }
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

  def sub8(start: Int, length: Int): Seq[Int] = {
    data.slice(start, start + length).toIndexedSeq
  }

  /**
   *
   * @param offset      starting at.
   * @param chunkLength how much per chunk.
   * @param nChunks     how many.
   * @return
   */
  def chunks(offset: Int, chunkLength: Int, nChunks: Int): Seq[Chunk] = {
    val size = chunkLength * nChunks
    data.slice(offset, offset + size)
      .grouped(chunkLength)
      .map(a => Chunk(a.toIndexedSeq)).toIndexedSeq

  }

//  /**
//   * File looks like:
//   * comment: comment\n
//   * stamp: ISO-8601\n as ISO-860
//   * size: data.length \n
//   * followed by one value per line.
//   *
//   * @param path where to save.
//   */
//  def save(path: Path): Unit = {
//    Using(new PrintWriter(Files.newBufferedWriter(path))) { writer: PrintWriter =>
////      writer.println(s"comment: \t$comment")
////      writer.println(s"stamp: \t${stamp.toString}")
////      writer.println(s"size: \t${data.length}")
//      data.zipWithIndex.foreach { case (v, i) =>
//        val s = f"$i%04d:$v%d"
//        writer.println(s)
//      }
//    }
//  }

}

object Memory extends LazyLogging {


    val r: Regex = """(.*):\s+(.*)""".r

    /**
     * Read from a file produced by [[Memory]].save().
     *
     * @param url where to read from.
     * @return the data or an exception.
     */
    def load(url: URL): Try[Memory] = {

      Using(new BufferedSource(url.openStream())) { bs =>
        var comment = ""
        var stamp = Instant.now()
        var size = -1
        val builder = Array.newBuilder[Int]

        bs.getLines().foreach {
          case r("comment", rvalue) =>
            comment = rvalue
          case r("stamp", rvalue) =>
            stamp = Instant.parse(rvalue)
          case r("size", rvalue) =>
            size = rvalue.toInt
          case line =>
            val value = line.drop(5) // get ride of index and colon
            builder += value.trim.toInt // get rid of index and colon
        }
        new Memory(builder.result())
      }
    }
}

