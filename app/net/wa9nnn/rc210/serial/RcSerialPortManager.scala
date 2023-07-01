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

package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.comm.SerialPortsSource

import java.nio.file.{Files, Paths}
import javax.inject.{Inject, Named, Singleton}

/**
 *
 * @param sFile             where to store currently selected [[ComPort]].
 * @param currentSerialPort holder for [[RcSerialPort]]. Avoids a circular dependency with [[net.wa9nnn.rc210.serial.comm.Rc210]].
 * @param serialPortsSource whats known from OS.
 */
@Singleton
class RcSerialPortManager @Inject()(@Named("vizRc210.serialPortsFile") sFile: String,
                                    currentSerialPort: CurrentSerialPort,
                                    serialPortsSource: SerialPortsSource = new SerialPortsSource()) extends LazyLogging {

  private val file = Paths.get(sFile)
  Files.createDirectories(file.getParent)

  try {
    selectPort(Files.readString(file))
  } catch {
    case e: Exception =>
      logger.error(e.getMessage)
  }

  def selectPort(portDescriptor: String): Unit =
    serialPortsSource().find(_.descriptor == portDescriptor)
      .foreach { comPort =>

        Files.writeString(file, comPort.descriptor)
        currentSerialPort.setCurrentPort(RcSerialPort(SerialPort.getCommPort(comPort.descriptor)))
      }
}


