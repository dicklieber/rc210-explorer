package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.command.Parsers.ParsedValues
import net.wa9nnn.rc210.serial.{Memory, SlicePos}

object CommandParser extends LazyLogging {
  def apply(commandId: Command, memory: Memory): ParsedValues = {

    val slicePos = SlicePos(commandId.getMemoryOffset, commandId.getMemoryLength)
    val slice = memory(slicePos)

    commandId.getValueType match {
      case ValueType.dtmf =>
        DtmfParser(commandId, slice)
      case ValueType.bool =>
        Seq(ItemValue(commandId, Seq((slice.head != 0).toString)))
      case ValueType.int8 =>
        Int8Parser(commandId, slice)
      case ValueType.int16 =>
        Int16Parser(commandId, slice)
      case ValueType.hangTime =>
        HangTimeParser(commandId, slice)

      //      case ValueType.int16 =>
      //      case ValueType.hangTime =>
      //      case ValueType.range =>
      case x =>
        throw new IllegalArgumentException(s"Unexpected valueType: $x")
    }

  }

}

