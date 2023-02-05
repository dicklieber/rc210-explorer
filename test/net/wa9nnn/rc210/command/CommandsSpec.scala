package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.fixtures.WithMemory

class CommandsSpec extends WithMemory with LazyLogging{

//  "Commands" should {
//    "first command" in {
//      val command = "*2108"
//      val first = Commands.commands(command)
//      first.commandId must beEqualTo (command)
//      first.slice must beEqualTo (MemorySlice(0,4))
//    }
//    "second command" in {
//      val command = "*2093"
//      val first = Commands.commandMap(command)
//      first.commandId must beEqualTo (command)
//      first.slice must beEqualTo (MemorySlice(4,6))
//    }
//  }


  "Parsing" >> {
    val d: Map[CommandId, ItemValue] = Commands.parse(memory)

//    logger.info(":::::::: sorted Result ::::::::")
//     d.values
//       .toSeq
//       .sorted
//       .foreach{iv: ItemValue =>
//         logger.info("sorted: {}", iv.toString)
//       }

    pending
  }
}
