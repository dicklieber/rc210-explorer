package net.wa9nnn

import net.wa9nnn.rc210.command.{Command, CommandParser, ItemValue}
import net.wa9nnn.rc210.data.Rc210Data

object Build {

  def apply() :Rc210Data = {
    val result: Array[ItemValue] = Command
      .values()
      .flatMap { command =>
        CommandParser(command, memory)
      }
    result.foreach(println(_))

    val rc210Data = Rc210Data(result.toIndexedSeq)

  }
}
