package net.wa9nnn.rc210.command

import scala.util.Try

sealed trait Parser {

  def apply(slice: Array[Int]): Try[ItemValue]
}

/**
 * Parse ASCII values into a string.
 */
private object DtmfParser extends Parser {
  def apply(slice: Array[Int]): Try[ItemValue] = {
    Try {
      ItemString(new String(
        slice
          .filter(_ != 0)
          .map(_.toChar)
      )
      )
    }
  }
}