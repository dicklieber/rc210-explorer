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

import com.fazecast.jSerialComm.{SerialPort, SerialPortDataListener}
import os.read.bytes

import scala.concurrent.duration.Duration

/**
 * This uses a [[SerialPortDataListener]] to get each message from the RC-210.
 * This works nicely for doing a Download from the RC-210.
 *
 * @param rcSerialPort provides access to the serial port.
 */
class RealEventBased(serialPort: SerialPort) extends RcOp(serialPort) with EventBased:
  serialPort.flushIOBuffers() // get rid of any left over crap.

  def addDataListener(serialPortDataListener: SerialPortDataListener): Unit =
    serialPort.addDataListener(serialPortDataListener)

  def send(string: String): Unit = {
    val s: String = s"\r${string}\r"
    val bytes = s.getBytes
    serialPort.writeBytes(bytes, bytes.length)
  }

trait RcEventBasedApi:

  def addDataListener(serialPortDataListener: SerialPortDataListener): Unit

  def send(string: String): Unit