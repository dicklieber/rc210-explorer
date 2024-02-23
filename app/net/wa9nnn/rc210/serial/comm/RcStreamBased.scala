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

import com.fazecast.jSerialComm.{SerialPort, SerialPortTimeoutException}
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.RcOperationResult
import net.wa9nnn.rc210.serial.comm.RcStreamBased.isTerminal

import java.io.OutputStream
import scala.concurrent.duration.Duration
import scala.io.BufferedSource
import scala.util.{Try, Using}

/**
 * Used for sending commands to the RC-210. Nicely handles multiple lines of response.
 *
 * @param rcSerialPort provides access to the serial port.
 */
class RcStreamBased(serialPort: SerialPort) extends RcOp(serialPort) with AutoCloseable with LazyLogging {
 
  serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0)
  private val source = new BufferedSource(serialPort.getInputStream, 50)
  private val outputStream: OutputStream = serialPort.getOutputStream

  private val lines: Iterator[String] = source.getLines()

  /**
   * This drains anything waiting in from [[BufferedSource]].
   *
   * @param request sends a request to the RC210
   * @return lines received up to a line starting with a [[terminalPrefaces]].
   * */
  def perform(request: String): RcResponse = {
    logger.trace("perform: {}", request)
    outputStream.write(request.getBytes)
    val resultBuilder = Seq.newBuilder[String]

    var line = ""
    while
      !isTerminal(line)
    do {
      line = lines.next()
      logger.trace("\tline: {}", line)
      resultBuilder += line
    }

    val result = resultBuilder.result()
    RcResponse(result)
  }

  def perform(requests: Seq[String]): Seq[RcOperationResult] = {
    requests.map(request => {
      val response: Try[RcResponse] = Try(perform(request))
      RcOperationResult(request, response)
    })
  }

  override def close(): Unit = {
    serialPort.removeDataListener()
    serialPort.closePort()
  }
}

object RcStreamBased {

  def isTerminal(line: String): Boolean =
    line.nonEmpty && terminalPrefaces.contains(line.head)

  def isOk(string: String): Boolean =
    string.head == terminalPrefaces.head

  private val terminalPrefaces: String = "+-"
}
