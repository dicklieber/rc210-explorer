package net.wa9nnn.rc210.command

import org.specs2.mutable.Specification

class CommandIdSpec extends Specification {
  "CommandId" >> {
    "tostring base" >> {
      val commandId: CommandId = CommandId("*123")
      val s = commandId.toString
      s must beEqualTo("*123")
    }
  }
}