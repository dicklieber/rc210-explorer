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

package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.ComPort

import java.io.PrintWriter

/**
 * All access to the RC210 goes through subclasses of this class.
 *
 * @param rcSerialPort provides access to the serial port.
 */
abstract class RcOp(rcSerialPort: RcSerialPort) extends LazyLogging {
  val serialPort: SerialPort = rcSerialPort.serialPort
  val comPort: ComPort = rcSerialPort.comPort
  if (serialPort.isOpen)
    logger.error(s"${comPort.toString} is already open!}")
  if (!serialPort.openPort())
    logger.error(s"$comPort did not open!}")
  private  val printWriter = new PrintWriter(serialPort.getOutputStream)

  def send(request:String): Unit = {
    printWriter.print(request + '\r')
    printWriter.flush()
  }
  def close():Unit =
    serialPort.closePort()
}
