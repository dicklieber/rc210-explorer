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

import java.io._
import scala.annotation.tailrec
import scala.util.Try

/**
 * Handles sending and receiving to the RC-210 via a serial port.
 *
 * @param comPort from [[RC210IO.listPorts]].
 */
class SerialPortOperation(comPort: ComPort) extends LazyLogging {
  private val serialPort: SerialPort = SerialPort.getCommPort(comPort.descriptor)
  serialPort.setBaudRate(19200)
  serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 5000, 1000)
  val opened: Boolean = serialPort.openPort()
  if (!opened) {
    logger.trace(s"Serialport: {} did not open!", comPort.toString)
    throw new IOException(s"Did not open $comPort")
  }
  val reader: BufferedReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream))
  val writer = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream))

  def preform(in: String): Try[String] = {
    Try {
      val prewriteReader = reader.ready()
      writer.write(in)
      writer.flush()

      readResponse()
    }
  }

  // Responses consist of one or more lines. Usually just one.
  // the last line begins with '+' or '-'
  private val terminaters = "-+"

  @tailrec
  private def readResponse(lines: Seq[String] = Seq.empty): String = {
    val line = reader.readLine()
    logger.trace("read line: {}", line)
    val soFar = lines :+ line
    val head = line.head
    if (terminaters.contains(head)) {
      logger.trace("\tGot terminator.")
      soFar mkString (" ") // done, take what we have so far and add this
    }
    else {
      logger.trace("\tNo +-, read another line.")
      readResponse(soFar) // read another.
    }
  }

  def close(): Unit = {
    logger.trace(s"Closing: {}", comPort.toString)
    writer.close()
    reader.close()
    serialPort.closePort()
  }
}