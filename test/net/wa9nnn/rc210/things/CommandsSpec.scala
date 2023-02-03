package net.wa9nnn.rc210.things

import net.wa9nnn.rc210.serial.MemorySlice
import org.specs2.mutable.Specification

class CommandsSpec extends Specification {

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
}
