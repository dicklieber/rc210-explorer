package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging

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
case class DataItem(name: String, maybeInt: Option[Int], value: String, debugInfo: DebugInfo) extends LazyLogging {
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
        logger.error(s"line: $debugInfo", e.getMessage)
        -1
    }
  }
}

object DataItem {
  val dataItem: Regex = """([^(]*)(?:\((\d+)\))?=(.*)""".r

  def apply(debugInfo: DebugInfo): Try[DataItem] = {
    Try {
      val dataItem(name, data, value) = debugInfo.line
      new DataItem(name, Option(data).map(_.toInt), value, debugInfo)
    }
  }

}

