package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.command.ItemValue.Values
import net.wa9nnn.rc210.command.Parsers.{ParsedValues, procSeq}
import net.wa9nnn.rc210.key.AlarmKey
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
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 1)
    Seq(procSeq(command, slice.data))
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
      case (b, port) <- ports.zipWithIndex
    } yield {
      val values: Seq[Int] = b.result()
      val iv = ItemValue(command, Seq.empty)
        .withPort(port)
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
    slice
      .data
      .grouped(1)
      .zipWithIndex
      .map { case (seq, i) =>
        procSeq(command, seq)
          .withPort(i)
      }
  }.toSeq
}

object PortInt16Parser extends Parser {
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 6)

    slice
      .data
      .grouped(2)
      .zipWithIndex
      .map { case (seq, port) =>
        procSeq(command, seq)
          .withPort(port)
      }
  }.toSeq
}

/**
 * A range
 * Data seems to be fixed e.g. "001090" we'll ignored null terminator. Would produce 1, 90
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
      .map { case (v, port) =>
        val value: Array[Char] = v.takeWhile(_ != 0).map(_.toChar).toArray
        ItemValue(command, new String(value))
          .withPort(port)
      }
      .toSeq
  }
}
object AlarmBoolParser extends Parser {
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 5)

    slice
      .data
      .grouped(1).zipWithIndex
      .map { case (v, i) =>
        procSeq(command, v)
          .withKey( AlarmKey(i + 4))
      }
      .toSeq
  }
}

object CwTonesParser extends Parser with LazyLogging {
  def apply(command: Command, slice: Slice): ParsedValues = {
    assert(slice.length == 12)
    logger.warn("//todo this one needs more consideration!")

    /*
    CW ID Programming
There are 2 CW ID messages, each of which may be programmed with up to 15 characters each (See Morse Code Character Table). They normally rotate as the Pending IDs. However if you have Speech Override ON and a signal appears on that port's receiver during a Voice ID, it will revert to CW and play CW message 2. To minimize disruption, it is recommended that you keep CW ID #2 as short as possible.
*8002xx-xx Program CWID #1 *8003xx-xx Program CWID #2
Examples
*8002 21 42 06 53 32 12 21 82 92 *8003 21 42 06 53 32 12 72
Program "AH6LE/AUX" into ID #1 Program" AH6LE/R" into ID #2
Note: If you exceed 15 characters, all characters that follow will be ignored.
Figure 6 below shows the relationships between the codes and the layout of a standard Touchtone© pad. As you can see, it makes it easier to remember the code for a particular character without having to look up codes (Q and Z are treated as special cases).
Fig 6 Morse Code Character Table
A21 U82 B22 V83 C23 W91 D31 X92 E32 Y93 F33 Z90 G41
H42 000 I43 101 J51 202 K52 303 L53 404 M61 505 N62 606 O63 707 P70 808 Q71 909 R 72
S 73 T 81
Word
Space 11 - 10 / 12 AR13 , 14 ?20 SK60
     */
    Seq.empty
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

  def procSeq(command: Command, in: Seq[Int]): ItemValue = {
    val value = in.length match {
      case 1 =>
        in.head
      case 2 =>
        in.head + in(1) * 256
      case x =>
        throw new IllegalArgumentException(s"Must be 1 or two Ints! but got: x")
    }
    val iv = ItemValue(command, value.toString)
    if (value > command.getMax)
      iv.withError(L10NMessage("toolarge"))
    else
      iv
  }

  def convertToBool(itemValue: ItemValue): ItemValue = {
    itemValue.copy(values = itemValue.values.map { v: String => (v != "0").toString }) // convert to "true" or "false"
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