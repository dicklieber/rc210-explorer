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
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, CellProvider, Row}
import controllers.routes
import net.wa9nnn.rc210.serial.{ComPort, NoPortSelected}

/**
 * Holds a [[SerialPort]]
 *
 * @param serialPort underlying
 */
case class RcSerialPort(private[comm] val serialPort: SerialPort, maybeVersion: Option[Rc210Version] = None) extends Ordered[RcSerialPort] with LazyLogging {


  def bytesAvailable(): Int = serialPort.bytesAvailable()

  //  private implicit val codec = Codec("US-ASCII")

  def write(request: String): Unit = {
    val bytes = s"\r$request\r".getBytes
    val length = bytes.length
    val bytesWritten = serialPort.writeBytes(bytes, length)
    if (bytesWritten != length)
      throw new NoPortSelected
  }

  def readBytes(buffer: Array[Byte], length: Int): Int = {
    serialPort.readBytes(buffer, length)
  }

  def addDataListener(listener: SerialPortDataListener): Unit = {
    serialPort.addDataListener(listener)
  }

  val comPort: ComPort = ComPort(serialPort)
  serialPort.setBaudRate(19200)

  /**
   *
   */
  def openStreamBased: RcStreamBased = {
    new RcStreamBased(this)
  }

  def openEventBased(): RcEventBased =
    new RcEventBased(this)


  def withVersion(rc210Version: Rc210Version): RcSerialPort =
    copy(maybeVersion = Option(rc210Version))

  override def compare(that: RcSerialPort): Int = this.comPort compareTo that.comPort

  override def toString: String = comPort.toString

  def toRow: Row = {
    Row(
      Cell(comPort.descriptor)
        .withUrl(routes.IOController.select(comPort.descriptor).url)
      ,
      comPort.friendlyName)
  }
}


case class Rc210Version(version: String, comPort: ComPort) extends CellProvider {
  override def toCell: Cell = Cell(version)
}

object Rc210Version {
  def apply(s: String, comPort: ComPort): Rc210Version = {
    new Rc210Version(s"${s.head}.${s.tail}", comPort)
  }
}

