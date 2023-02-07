package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.serial.Slice

import scala.util.Try

sealed trait Parser {

  def apply(commandId: CommandId, slice: Slice): ItemValue
}

/**
 * Parse ASCII values into a string.
 */
object DtmfParser extends Parser {
  def apply(commandId: CommandId, slice: Slice): ItemValue = {
    ItemValue(commandId, Try {
      val max: Int = commandId.getMax
      val str = new String(slice.data
        .takeWhile(_ != 0)
        .map(_.toChar)
        .toArray
      )
      if (str.length > max)
        throw L10NParseException("tooLong", str, max)
      Seq(str)
    }
    )
  }

}

object Int8Parser extends Parser {
  def apply(commandId: CommandId, slice: Slice): ItemValue = {
    ItemValue(commandId, Try {
      val max: Int = commandId.getMax
      val v = slice.head
      if (v > max)
        throw new IllegalArgumentException(s"")

      val str = new String(slice.data
        .takeWhile(_ != 0)
        .map(_.toChar)
        .toArray
      )
      if (str.length > max)
        throw L10NParseException("tooLarge", str, max)
      Seq(str)
    }
    )
  }
}

object Int16Parser extends Parser {
  def apply(commandId: CommandId, slice: Slice): ItemValue = {
    ItemValue(commandId, Try {
      val max: Int = commandId.getMax

      val iterator = slice.iterator
      val v = iterator.next() + iterator.next() * 256
      if (v > max)
        throw new IllegalArgumentException(s"")

      val str = new String(slice.data
        .takeWhile(_ != 0)
        .map(_.toChar)
        .toArray
      )
      if (str.length > max)
        throw L10NParseException("tooLarge", str, max)
      Seq(str)
    }
    )
  }
}

case class L10NMessage(messageKey: String, args: List[String] = List.empty)


case class L10NParseException(l10NError: L10NMessage) extends Exception

object L10NParseException {
  def apply(messageKey: String, args: Any*): L10NParseException = {
    new L10NParseException(L10NMessage(messageKey, args.map(_.toString).toList))
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