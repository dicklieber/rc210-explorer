/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wa9nnn.rc210

import net.wa9nnn.rc210.command.{Command, CommandParser, ItemValue}
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.data.macros.MacroExtractor
import net.wa9nnn.rc210.data.schedules.ScheduleExtractor
import net.wa9nnn.rc210.data.vocabulary.{MessageMacro, MessageMacroExtractor}
import net.wa9nnn.rc210.serial.{Memory, MemoryArray}

import java.io.InputStream
import javax.inject.Singleton
import scala.util.Using

@Singleton
class DataProvider() {

  val rc210Data: Rc210Data = {

    Using(getClass.getResourceAsStream("/MemFixedtxt.txt")) { stream: InputStream =>
      val memory: Memory = MemoryArray(stream).get

      val itemValues: Array[ItemValue] = Command
        .values()
        .flatMap { command =>
          CommandParser(command, memory)
        }

      val macros = MacroExtractor(memory)
      val schedules = ScheduleExtractor(memory)
      val messageMacros: Seq[MessageMacro] = MessageMacroExtractor(memory)


      Rc210Data(itemValues.toIndexedSeq, macros, schedules,messageMacros)

    }.get
  }
}
