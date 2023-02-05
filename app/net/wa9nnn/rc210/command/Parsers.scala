package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.serial.Slice

import scala.util.Try

sealed trait Parser {

  def apply(slice: Slice): Try[String]
}

/**
 * Parse ASCII values into a string.
 */
private object DtmfParser extends Parser {
  def apply(slice: Slice): Try[String] = {
    Try{
      val str = new String(slice.data
        .takeWhile(_ != 0)
        .map(_.toChar)
        .toArray
      )
      str
    }
  }
}


///**
// * Parse ASCII values into a string.
// */
//private object BooleanParser extends Parser {
//  def apply(slice: Array[Int]): Try[ItemValue] = {
//    Try {
//      assert(slice.length == 1)
//      ItemBoolean(slice.head != 0)
//    }
//  }
//}