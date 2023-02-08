package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.Parsers.ParsedValues
import net.wa9nnn.rc210.fixtures.WithMemory

class CommandSpec extends WithMemory {
  "CommandId" >> {
    "tostring base" >> {
      val ids = Command.values()

      ids.foreach { command =>
        println(s"Command  $command")
        val values1: ParsedValues = CommandParser(command, memory)
        println(s"\t$values1")
      }


      pending
    }
  }
}


class testCmd extends WithMemory {
  "SitePrefix" >> {
    "OK" >> {
      val itemValue: ParsedValues = CommandParser(Command.SitePrefix, memory)
      itemValue.head.head must beEqualTo("ABC")
    }
  }
  "SayHours" >> {
    val itemValue = CommandParser(Command.SayHours, memory)
    itemValue.head.head must beEqualTo("true")
  }
  "Hangtimes" >> {
    val itemValue: ParsedValues = CommandParser(Command.Hangtime, memory)
    itemValue must haveSize(3)

    itemValue.head.toString must beEqualTo ("commandId: Hangtime value: 40, 40, 144 port: 1)")
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