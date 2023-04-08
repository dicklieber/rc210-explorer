package net.wa9nnn.rc210.command




///**
// * Parse ASCII values into a string.
// */
//object DtmfParser extends Parser {
//  def apply(commandId: Command, slice: Slice): ParsedValues = {
//    Seq(ItemValue(commandId, Try {
//      val max: Int = commandId.getMax
//      val str = new String(slice.data
//        .takeWhile(_ != 0)
//        .map(_.toChar)
//        .toArray
//      )
//      if (str.length > max)
//        throw L10NParseException("tooLong", str, max)
//      Seq(str)
//    }
//    )
//    )
//  }
//
//}









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