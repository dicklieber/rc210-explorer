package net.wa9nnn.rc210

import net.wa9nnn.rc210.command.{Command, CommandParser, ItemValue}
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.data.macros.{Macro, MacroExtractor}
import net.wa9nnn.rc210.data.schedules.ScheduleExtractor
import net.wa9nnn.rc210.serial.{Memory, MemoryArray}

import java.io.InputStream
import javax.inject.Singleton
import scala.util.{Try, Using}

@Singleton
class DataProvider() {

  val rc210Data: Rc210Data = {

    Using(getClass.getResourceAsStream("/MemFixedtxt.txt")) { stream: InputStream =>
      val memory: Memory = MemoryArray(stream).get

      val result: Array[ItemValue] = Command
        .values()
        .flatMap { command =>
          CommandParser(command, memory)
        }
      result.foreach(println(_))

      val macros = MacroExtractor(memory)
      val schedules = ScheduleExtractor(memory)


      Rc210Data(result.toIndexedSeq, macros, schedules)

    }.get
  }
}
