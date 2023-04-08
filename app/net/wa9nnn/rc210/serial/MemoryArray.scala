package net.wa9nnn.rc210.serial

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, Path}
import java.time.Instant
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
case class MemoryArray(data: Array[Int], comment: String = "", stamp: Instant = Instant.now()) {

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
      data.zipWithIndex.foreach { case (v, i) =>
        val s = f"$i%04d:$v%d"
        writer.println(s)
      }
    }
  }
}


object MemoryArray {
  // RC-210 download data has lines like
  val r: Regex = """(.*):\s+(.*)""".r

  /**
   * Read from a file produced by MemoryArray.save().
   *
   * @param url where to read from.
   * @return the data or an exception.
   */
  def apply(url: URL): Try[MemoryArray] = {

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
          line.drop(5) // get ride of index and colon
          builder += line.drop(5).trim.toInt // get rid of index and colon
      }
      MemoryArray(builder.result(), comment, stamp)
    }
  }
}

