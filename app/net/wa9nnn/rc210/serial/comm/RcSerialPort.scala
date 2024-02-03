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
import com.wa9nnn.wa9nnnutil.tableui.{Cell, CellProvider, Row}
import controllers.routes
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.serial.{NoPortSelected, SerialPortOpenException}
import play.twirl.api.Html
import views.html.nameEditKey

import scala.util.{Failure, Success, Try, Using}

/**
 * Holds a [[SerialPort]]
 * And some low-level behaviour..
 *
 * @param serialPort underlying
 */
class RcSerialPort(val serialPort: SerialPort) extends LazyLogging:

  logger.debug(toString)
  serialPort.setBaudRate(19200)

  def version: Try[String] =
    val html: Html = nameEditKey(Key.portKeys.head)
    drain()
    Using(startStreamBased) { (streamBased: RcStreamBased) =>
      val response = streamBased.perform("\r\r1GetVersion\r")

      val lines = response.lines
      logger.whenWarnEnabled {
        if (lines.nonEmpty)
          logger.debug("Getting version:")

        lines.foreach(line =>
          logger.debug(s"\t:$line")
        )
      }
      val versionLine: String = lines.head
      val left = versionLine.head
      val right: String = versionLine.drop(1)
      val r = s"$left.$right"
      r
    }

  def write(request: String): Unit = {
    val bytes = s"\r$request\r".getBytes
    val length = bytes.length
    val bytesWritten = serialPort.writeBytes(bytes, length)
    if (bytesWritten != length)
      throw new NoPortSelected
  }

  def open(): Unit =
    if (serialPort.isOpen)
      logger.warn("{} is alerady open!", toString)

    val bool = serialPort.openPort()
    if (!bool)
      logger.error(s"Did not oppen: $serialPort")

  def close(): Unit =
    serialPort.closePort()

  def readBytes(length: Int): Array[Byte] =
    val buffer = new Array[Byte](length)
    serialPort.readBytes(buffer, length)
    buffer

  def startEventBased(): RcEventBasedOp =
    open()
    new RcEventBasedOp(this)

  def startStreamBased: RcStreamBased =
    open()
    new RcStreamBased(this)

  def drain(): Unit =
    open()
    val available = serialPort.bytesAvailable()
    if (available > 0)
      logger.info("Draining {} bytes from buffer.", available)
      readBytes(available)
    close()

  override def toString: String =
    val description = serialPort.getPortDescription
    val systemPortName = serialPort.getSystemPortName
    s"description: $description systemPortName: $systemPortName"




//
//case class Rc210Version(version: String, comPort: ComPort) extends CellProvider {
//  override def toCell: Cell = Cell(version)
//}
//
//object Rc210Version {
//  def apply(s: String, comPort: ComPort): Rc210Version = {
//    new Rc210Version(s"${s.head}.${s.tail}", comPort)
//  }
//}

