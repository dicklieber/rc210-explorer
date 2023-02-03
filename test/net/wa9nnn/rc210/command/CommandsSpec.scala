package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.fixtures.WithMemory
import net.wa9nnn.rc210.serial.MemorySlice
import net.wa9nnn.rc210.command.Commands.Command

import scala.util.Try

class CommandsSpec extends WithMemory {

  "Commands" should {
    "first command" in {
      val command = "*2108"
      val first = Commands.commandMap(command)
      first.command must beEqualTo (command)
      first.slice must beEqualTo (MemorySlice(0,4))
    }
    "second command" in {
      val command = "*2093"
      val first = Commands.commandMap(command)
      first.command must beEqualTo (command)
      first.slice must beEqualTo (MemorySlice(4,6))
    }
  }


  "Parsing" >> {
    val d: Map[Command, Try[ItemValue]] = Commands.parse(memory)
    d must haveSize(2)
  }
}
