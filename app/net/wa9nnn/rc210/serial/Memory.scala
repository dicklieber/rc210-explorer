package net.wa9nnn.rc210.serial

import net.wa9nnn.rc210.data.FieldKey
import play.api.libs.json.{Json, OFormat}

import java.io.{InputStream, PrintWriter}
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
case class MemoryArray(data: Array[Int], comment: String = "", stamp: Instant = Instant.now()) extends Memory {

  data.view(42)


  /**
   *
   * @param slice part of [[Memory]] we are interested in.
   * @return requested [[Array]].
   */
  def apply(slice: SlicePos): Slice = {
    Slice(data.slice(slice.offset, slice.until).toIndexedSeq, slice)
  }

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


/**
 * Specifies a position and length in [[Memory]]
 *
 * @param offset in [[Memory]].
 * @param length how much to slice. 0
 * @param name   as used in [[akka.io.Dns.Command]] and, pefhaps,  other places.
 */
case class SlicePos(offset: Int = 0, length: Int = 1, fieldKey:Option[FieldKey] = None) {
  def until: Int = offset + length

//  def apply(requested: Int): SlicePos = {
//    SlicePos(offset + length, requested)
//  }

  override def toString: String = s"$offset to $until"
}

object SlicePos {
  val r: Regex = """//([^\d^\-]+)\s.*[\s\-]+(\d+)[\s\-]+(\d+)""".r

  /**
   * WebRCP php essentially documents the eeprom dump with comments like: "//Macro - 1985-2624"
   *
   * @param phpComment e.g. "//DTMFEnable - 70-72"
   * @return
   */
  def apply(phpComment: String): SlicePos = {
    val r(name, sOffset, sEnd) = phpComment

    val offset = sOffset.toInt
    val end = sEnd .toInt
    val len = end - offset
    new SlicePos(offset, len + 1)
  }

  implicit val fmtSlicePos: OFormat[SlicePos] = Json.format[SlicePos]
}

/**
 * result from applying a [[SlicePos]] to [[Memory]]
 *
 * @param data     from the pos in [[Memory]]
 * @param slicePos that was requested.
 */
case class Slice(data: Seq[Int] = Seq.empty, slicePos: SlicePos = SlicePos()) {
  def length: Int = data.length

  override def toString: String = {
    s"$slicePos => ${data.mkString(",")}"
  }

  def iterator: Iterator[Int] = {
    data.iterator
  }

  def head: Int = {
    data.head
  }
}

object Slice {
  /**
   * Handy for unit tests.
   *
   * @param csv e.g. 2,4,42
   * @return
   */
  def apply(csv: String): Slice = {
    val seq: Seq[Int] = csv
      .split(',')
      .map { s: String =>
        s.trim.toInt
      }.toIndexedSeq
    new Slice(seq,
      SlicePos())
  }

  implicit val fmtSlice: OFormat[Slice] = Json.format[Slice]
}

object MemoryArray {
  // RC-210 download data has lines like
  val r: Regex = """(.*):\s+(.*)""".r

  /**
   * Read from a file produced by [[MemoryArray.save()]].
   *
   * @param path where to read from.
   * @return the data or an exception.
   */
  def apply(path: Path): Try[MemoryArray] = {
    apply(Files.newInputStream(path))
  }

  def apply(inputStream: InputStream): Try[MemoryArray] = {
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
          line.drop(5) // get reide of index and colon
          builder += line.drop(5).toInt // get rid of index and colon
      }
      MemoryArray(builder.result(), comment, stamp)
    }
  }
}

trait Memory {
  def apply(slice: SlicePos): Slice
}