package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.command.Parsers.{ParsedValues, convertToBool}
import net.wa9nnn.rc210.serial.{Memory, SlicePos}

object CommandParser extends LazyLogging {
  def apply(command: Command, memory: Memory): ParsedValues = {

    val slicePos = SlicePos(command.getMemoryOffset, command.getMemoryLength)
    val slice = memory(slicePos)

    command.getValueType match {
      case ValueType.dtmf =>
        DtmfParser(command, slice)
      case ValueType.bool =>
        Seq(ItemValue(command, Seq((slice.head != 0).toString)))
      case ValueType.int8 =>
        Int8Parser(command, slice)
      case ValueType.int16 =>
        Int16Parser(command, slice)
      case ValueType.hangTime =>
        HangTimeParser(command, slice)
      case ValueType.portInt8 =>
        PortInt8Parser(command, slice)
      case ValueType.portBool =>
        PortInt8Parser(command, slice).map { iv =>
          convertToBool(iv)
        }
      case ValueType.portInt16 =>
        PortInt16Parser(command, slice)
      case ValueType.guestMacro =>
        GuestMacroSubsetParser(command, slice)
      case ValueType.portUnlock =>
        PortUnlockParser(command, slice)
      case ValueType.cwTones =>
        CwTonesParser(command, slice)
      case ValueType.alarmBool =>
        AlarmBoolParser(command, slice)

      case ValueType.unused =>
        // Not used
        Seq.empty

      //      case ValueType.int16 =>
      //      case ValueType.hangTime =>
      //      case ValueType.range =>
      case x =>
        throw new IllegalArgumentException(s"Unexpected valueType: $x")
    }

  }

}

