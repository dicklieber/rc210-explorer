package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.fixtures.WithMemory
import play.api.libs.json.Json

class CommandsSpec extends WithMemory with LazyLogging {
  "All commands " >> {
    val result: Array[ItemValue] = Command
      .values()
      .flatMap { command =>
        CommandParser(command, memory)
      }
    result.foreach(println(_))

    val rc210Data = Rc210Data(result.toIndexedSeq, Seq.empty, Seq.empty)

    val sJson = Json.prettyPrint(Json.toJson(rc210Data))
    println(sJson)
    pending
  }

}