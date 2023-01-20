package net.wa9nnn.model

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.RawDataLine

import scala.util.Try
import scala.util.matching.Regex

/**
 * Internalized version of one data line in a section.
 * MacroToRun(3)=03
 * |||||||||| |  +  = value
 * |||||||||| + ==== number
 * ++++++++++ ====== name
 *
 * @param name           of item.
 * @param maybeInt       number that may be inside of parenthesis.
 * @param value          rvalue
 * @param debugInfo      perhaps useful.
 */
case class DataItem(name: String, maybeInt: Option[Int], value: String, debugInfo: RawDataLine) extends LazyLogging {
  def dump(): Unit = {
    logger.info(s"\t$name\t${maybeInt.getOrElse("-")}\t$value")
  }

  /**
   *
   * @return the number
   * @throws java.util.NoSuchElementException – if the maybeInt is empty.
   */
  def number: Int = {
    try {
      maybeInt.get
    } catch {
      case e: Exception =>
        //        logger.error(s"line: $debugInfo", e.getMessage)
        throw e
    }
  }
}

object DataItem {
  val dataItem: Regex = """([^(]*)(?:\((\d+)\))?=(.*)""".r

  def apply(rawDataLine: RawDataLine): DataItem = {

    val dataItem(name, number, value) = rawDataLine.line
    new DataItem(name, Option(number).map(_.toInt), value, rawDataLine)
  }

}

