package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.ItemValue.Values
import net.wa9nnn.rc210.command.Parsers.ParsedValues
import net.wa9nnn.rc210.serial.Slice

import scala.util.Try


sealed trait Parser {

  def apply(commandId: Command, slice: Slice): ParsedValues
}

/**
 * Parse ASCII values into a string.
 */
object DtmfParser extends Parser {
  def apply(commandId: Command, slice: Slice): ParsedValues = {
    Seq(ItemValue(commandId, Try {
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
    )
  }

}

object Int8Parser extends Parser {
  def apply(commandId: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 1)

    Seq(ItemValue(commandId, Try {
      val max: Int = commandId.getMax
      val v = slice.head
      if (v > max)
        throw new IllegalArgumentException(s"")

      val str = v.toString
      if (v > max)
        throw L10NParseException("tooLarge", str, max)
      Seq(str)
    }
    ))
  }
}

object Int16Parser extends Parser {
  def apply(commandId: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 2)
    Seq(ItemValue(commandId, Try {
      val max: Int = commandId.getMax

      val iterator = slice.iterator
      val v = iterator.next() + iterator.next() * 256
      if (v > max)
        throw new IllegalArgumentException(s"")
      Seq(v.toString)
    }
    ))
  }
}

object HangTimeParser extends Parser {
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 9)

    val max: Int = command.getMax
    val iterator = slice.iterator
    val ports =
      Seq(Seq.newBuilder[Int],
        Seq.newBuilder[Int],
        Seq.newBuilder[Int])
    for {
      sub <- 1 to 3
      port <- 1 to 3
    } yield {
      ports(port - 1) += iterator.next()
    }

    for {
      case (b, i) <- ports.zipWithIndex
    } yield {
      val values: Seq[Int] = b.result()
      val iv = ItemValue(command, Seq.empty)
        .withVIndex(VIndex.port(i + 1))
      if (values.exists(_ > max))
        iv.withError(L10NMessage("toolarge"))
      else
        iv
    }
  }
}

object PortInt8Parser extends Parser {
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 3)

    val max: Int = command.getMax
    val iterator = slice.iterator
    for {
      port <- 1 to 3
    } yield {
      val value = iterator.next()
      val iv = ItemValue(command, value.toString)
        .withVIndex(VIndex.port(port))
      if (value > max) {
        iv.withError(L10NMessage("toolarge"))
      } else {
        iv
      }
    }
  }
}

object PortInt16Parser extends Parser {
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 6)
    val max: Int = command.getMax
    val iterator = slice.iterator
    for {
      port <- 1 to 3
    } yield {
      val value = iterator.next() + iterator.next() * 256
      val iv = ItemValue(command, value.toString)
        .withVIndex(VIndex.port(port))
      if (value > max) {
        iv.withError(L10NMessage("toolarge"))
      } else {
        iv
      }
    }
  }
}

/**
 * A range
 * Data seems to be fixed e.g. "001090" we'll ignored null terminator. Wouod produce 1, 90
 */
object GuestMacroSubsetParser extends Parser {
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 7)

    val s: Values = slice
      .data
      .take(6)
      .map(_.toChar).toArray
      .grouped(3)
      .map(new String(_))
      .toSeq
    Seq(ItemValue(command, s))
  }
}

object PortUnlockParser extends Parser {
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 27)

    slice
      .data
      .grouped(9).zipWithIndex
      .map { case (v, i) =>
        val value: Array[Char] = v.takeWhile(_ != 0).map(_.toChar).toArray
        ItemValue(command, new String(value))
          .withVIndex(VIndex.port(i + 1))
      }
      .toSeq
  }
}


case class L10NMessage(messageKey: String, cssClass: String = "", args: List[String] = List.empty)


case class L10NParseException(l10NError: L10NMessage) extends Exception

object L10NParseException {
  def apply(messageKey: String, args: Any*): L10NParseException = {
    val value: Seq[String] = args.map(_.toString)
    val message = L10NMessage(messageKey, "errpr", value.toList)
    new L10NParseException(message)
  }
}

object Parsers {
  type ParsedValues = Seq[ItemValue]

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