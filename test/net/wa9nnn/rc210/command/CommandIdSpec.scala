package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.fixtures.WithMemory
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import org.specs2.mutable.Specification

import scala.util.{Failure, Success}

class CommandIdSpec extends Specification {
  "CommandId" >> {
    "tostring base" >> {
      val ids = CommandId.values()
      pending
    }
  }
}


class testCmd extends WithMemory {
  "SitePrefix" >> {
    "OK" >> {
      val itemValue = CommandParser(CommandId.SitePrefix, memory)
      itemValue.values.head must beEqualTo("ABC")
    }
  }
  "SayHours" >> {
    val itemValue = CommandParser(CommandId.SayHours, memory)
    itemValue.values.head must beEqualTo("true")
  }


  //  def dito (commandId: CommandId, memory: Memory): ItemValue={
  //
  //    val slicePos = SlicePos(commandId.getMemoryOffset, commandId.getMemoryLength)
  //    val slice = memory(slicePos)
  //
  //    commandId.getValueType match {
  //      case ValueType.dtmf =>
  //        val triedValue = DtmfParser(slice)
  //        ItemValue(commandId, triedValue)
  //      case ValueType.bool =>
  //      case ValueType.int8 =>
  //      case ValueType.int16 =>
  //      case ValueType.hangTime =>
  //      case ValueType.range =>
  //      case x =>
  //
  //    }
  //
  //  }

}