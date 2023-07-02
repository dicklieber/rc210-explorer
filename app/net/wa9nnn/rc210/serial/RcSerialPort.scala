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

import com.fazecast.jSerialComm.{SerialPort, SerialPortDataListener, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Row, RowSource}
import controllers.routes

import scala.annotation.unused

/**
 * Holds a [[SerialPort]]
 *
 * @param serialPort underlying
 */
@unused
case class RcSerialPort(serialPort: SerialPort) extends Ordered[RcSerialPort] with LazyLogging {
  def addDataListener(listener: SerialPortDataListener): Unit = {
    serialPort.addDataListener(listener)
  }

  val comPort: ComPort = ComPort(serialPort)
  serialPort.setBaudRate(19200)

  /**
   *
   * @return a usable serial port.
   */
  def openPort(): OpenedSerialPort = {
    OpenedSerialPort(this)
  }

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


case class OpenedSerialPort(rcSerialPort: RcSerialPort) {
  lazy val comPort: ComPort = rcSerialPort.comPort

  private val serialPort: SerialPort = rcSerialPort.serialPort
  serialPort.openPort()

  def readBytes(array: Array[Byte], bytesToRead: Int): Int = serialPort.readBytes(array, bytesToRead)

  def bytesAvailable(): Int = serialPort.bytesAvailable()

  def write(string: String): Unit = {
    val bytes = string.getBytes()
    if (serialPort.writeBytes(bytes, bytes.length) == -1) {
      if (!serialPort.isOpen)
        throw new Exception(s"SerialPort not open writing: $string")
    }
  }

  def addDataListener(listner: SerialPortMessageListenerWithExceptions): Boolean = serialPort.addDataListener(listner)

  def removeDataListener(): Unit = serialPort.removeDataListener()

  def open(): Unit = if (!serialPort.openPort())
    throw new Exception(s"${rcSerialPort.comPort}comPort not opened!")

  def close(): Unit = serialPort.closePort()


}