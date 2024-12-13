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
import net.wa9nnn.rc210.serial.comm.RealStreamBased.isTerminal
import net.wa9nnn.rc210.util.expandControlChars

import java.io.{InputStream, OutputStream}
import java.nio.charset.MalformedInputException
import scala.collection.mutable
import scala.io.BufferedSource
import scala.util.*

/**
 * Used for sending commands to the RC-210. Nicely handles multiple lines of response.
 *
 * @param rcSerialPort provides access to the serial port.
 */
class RealStreamBased(serialPort: SerialPort) extends RcOp(serialPort) with StreamBased with AutoCloseable with LazyLogging:

  serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0)
  val inputStream: InputStream = serialPort.getInputStream
  private val outputStream: OutputStream = serialPort.getOutputStream

  private def readLine: String =
    val lineBuilder: mutable.Builder[Byte, Seq[Byte]] = Seq.newBuilder[Byte]
    boundary:
      while (true) {
        inputStream.read() match
          case -1 =>
            boundary.break(lineBuilder.result())
          case 0xfe=>

          case 0xa =>
            boundary.break(lineBuilder.result())
          case b =>
            lineBuilder.addOne(b.toByte)
      }
    val bytes: Array[Byte] = lineBuilder.result().toArray
    val chars: Array[Char] = bytes.map(_.toChar)
    String(chars)

  /**
   * This drains anything waiting in from [[BufferedSource]].
   *
   * @param request sends a request to the RC210
   * @return what we got.
   * */
  def perform(request: String): RcResponse =
    val requestWithCr: String = request :+ '\r'
    outputStream.write(requestWithCr.getBytes)
    outputStream.flush()
    //    val inLines: Iterator[String] = source.getLines()

    val linesBuilder = Seq.newBuilder[String]
    try {
      var line: String = ""
      while (!isTerminal(line)) {
        try {
          line = readLine
          logger.trace("line: {}", line)
          linesBuilder += line
        }
        catch
          case e: MalformedInputException =>
            logger.warn("IllformedInput probably startup non-sense", e)
      }
    }
    catch
      case e: Exception =>
        logger.error(s"Reading response for ${expandControlChars(requestWithCr)}", e)

    RcResponse(requestWithCr, linesBuilder.result())

  def perform(requests: Seq[String]): Seq[RcResponse] =
    requests.map(request => {
      perform(request)
    })

  override def close(): Unit =
    serialPort.removeDataListener()
    serialPort.closePort()

object RealStreamBased:
  def isTerminal(line: String): Boolean =
    line.nonEmpty && terminalPrefaces.contains(line.head)

  val terminalPrefaces: String = "+-"