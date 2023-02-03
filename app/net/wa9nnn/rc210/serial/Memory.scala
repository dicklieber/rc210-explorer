package net.wa9nnn.rc210.serial

import java.io.{InputStream, PrintWriter}
import java.nio.file.{Files, Path}
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import scala.io.BufferedSource
import scala.util.matching.Regex
import scala.util.{Try, Using}

/**
 * Manages an array of Ints. Must be in range of 0 to 255.
 *
 * @param data    the data. Usually downloaded RC-210.
 * @param comment Anything.
 * @param stamp   when we did this.
 */
case class Memory(data: Array[Int], comment: String = "", stamp: Instant = Instant.now()) {
  /**
   *
   * @param slice part of [[Memory]] we are interested in.
   * @return requested [[Array]].
   */
  def apply(slice: MemorySlice): Array[Int] = data.slice(slice.offset, slice.until)

  /**
   * File looks like:
   * comment: comment\n
   * stamp: ISO-8601\n as ISO-860
   * size: data.length \n
   * followed by one value per line.
   *
   * @param path where to save.
   */
  def save(path: Path): Unit = {
    Using(new PrintWriter(Files.newBufferedWriter(path))) { writer: PrintWriter =>
      writer.println(s"comment: \t$comment")
      writer.println(s"stamp: \t${stamp.toString}")
      writer.println(s"size: \t${data.length}")
      data.foreach { x =>
        writer.println(x.toString)
      }
    }
  }
}

/**
 * Used to calculate walking through [[Memory]]
 */
class Slicer() {
  private val pos = new AtomicInteger()

  def apply(length: Int): MemorySlice = {
    MemorySlice(pos.getAndAdd(length), length)
  }
}

/**
 * Specifies a part (slice) of memory.
 *
 * @param offset in [[Memory]].
 * @param length how much to slice.
 */
case class MemorySlice(offset: Int = 0, length: Int = 0) {
  def until: Int = offset + length

  def apply(requested: Int): MemorySlice = {
    MemorySlice(offset + length, requested)
  }
}

object Memory {
  // RC-210 download data has lines like
  val r: Regex = """(.*):\s+(.*)""".r

  /**
   * Read from a file produced by [[Memory.save()]].
   *
   * @param path where to read from.
   * @return the data or an exception.
   */
  def apply(path: Path): Try[Memory] = {
    apply(Files.newInputStream(path))
  }

  def apply(inputStream: InputStream): Try[Memory] = {
    Using(new BufferedSource(inputStream)) { bs =>
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
          builder += line.toInt
      }
      Memory(builder.result(), comment, stamp)
    }
  }

}