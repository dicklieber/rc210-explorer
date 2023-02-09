package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.Parsers.ParsedValues
import net.wa9nnn.rc210.fixtures.WithMemory

class CommandSpec extends WithMemory {
  "CommandId" >> {
    "tostring base" >> {
      val ids = Command.values()

      ids.foreach { command =>
        val pv: ParsedValues = CommandParser(command, memory)
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

    itemValue.head.toString must beEqualTo ("commandId: Hangtime value: 11, 12, 13 port: 1)")
  }


}