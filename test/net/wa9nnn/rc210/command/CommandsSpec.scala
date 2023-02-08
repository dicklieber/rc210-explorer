package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.command.Parsers.ParsedValues
import net.wa9nnn.rc210.fixtures.WithMemory

class CommandsSpec extends WithMemory with LazyLogging {
  "All commands " >> {
    val result = Command
      .values()
      .flatMap{ command =>
        CommandParser(command, memory)
      }
    result.foreach(println(_))
    pending
  }

}