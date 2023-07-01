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

import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.OpenedSerialPort
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse

import java.time.{Duration, Instant}
import scala.collection.mutable
import scala.concurrent.{Future, Promise}

case class Transaction(request: String, openedSerialPort: OpenedSerialPort, start: Instant = Instant.now()) extends SerialPortMessageListenerWithExceptions with LazyLogging {

  assert(openedSerialPort.rcSerialPort.serialPort.isOpen, "Serial port not open!")
  openedSerialPort.addDataListener(this)
  private val lineBuilder: mutable.Builder[String, Seq[String]] = Seq.newBuilder[String]

  /**
   *
   * @param response one line from RC210.
   * @return true if the promise was completed.
   */
  def collect(response: String): Boolean = {
    lineBuilder += response
    response.head match {
      case '+' =>
        promise.success(lineBuilder.result())
        openedSerialPort.removeDataListener()
        true
      case '-' =>
        promise.success(lineBuilder.result())
        openedSerialPort.removeDataListener()
        true
      case _ =>
        false
    }
  }

  override def catchException(e: Exception): Unit = {
    logger.error(s"$openedSerialPort", e)
  }

  override def getMessageDelimiter: Array[Byte] = Array('\n')

  override def delimiterIndicatesEndOfMessage(): Boolean = true

  override def getListeningEvents: Int = SerialPort.LISTENING_EVENT_DATA_RECEIVED

  /**
   * Invoked by jSerialComm on it's own thread.
   *
   * @param event from [[SerialPort]].
   */
  override def serialEvent(event: SerialPortEvent): Unit = {
    val receivedData: Array[Byte] = event.getReceivedData
    val response = new String(receivedData).trim
    collect(response)
  }

  private val promise = Promise[Seq[String]]()
  val future: Future[RcResponse] = promise.future
  openedSerialPort.write(request)

  override def toString: String = {
    s"On: $openedSerialPort Request: $request Duration: ${Duration.between(start, Instant.now())}"
  }
}