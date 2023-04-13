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

package net.wa9nnn.rc210.io

import com.fazecast.jSerialComm.SerialPort
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.serial.{Memory, RC210Data}
import net.wa9nnn.rc210.util.EramStatus

import java.nio.file.Files

class DatFileSpec extends WithTestConfiguration {


  cleanDirectory()

  private val datFile = new DatFile(config)
  "DatFileSpec" should {
    "save" in {
      ok
    }

    "from rc210data" >> {
      Files.createDirectories(datFile.memoryFile.getParent)
      Files.write(datFile.memoryFile, "Just to exist".getBytes)
      val main = Array.fill(10)(42)
      val ext = Array.fill(5)(142)


      val rC210Data = new RC210Data(main, ext, new EramStatus("com3"), SerialPort.getCommPorts.head)
      val memory = datFile.apply(rC210Data)
      memory.length must beEqualTo(main.length + ext.length)
    }

  }
}
