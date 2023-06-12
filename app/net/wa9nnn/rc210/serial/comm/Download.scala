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

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.ComPort

import scala.concurrent.{Future, Promise, blocking}
import scala.util.matching.Regex

object Download extends LazyLogging {
  private val parser: Regex = """(\d+),(\d+)""".r

  def apply(comPort: ComPort): Future[Seq[Int]] = {
    var count = 0
    val promise: Promise[Seq[Int]] = Promise[Seq[Int]]()
    val builder = Seq.newBuilder[Int]
    val serialPort = SerialPort.getCommPort(comPort.descriptor)
    serialPort.setBaudRate(19200)
    serialPort.openPort()

    def write(s: String = ""): Unit = {
      val bytes = (s + '\r').getBytes
      serialPort.writeBytes(bytes, bytes.length)
    }

    for (_ <- 0 to 3) {
      write()
      Thread.sleep(100)
    }

    serialPort.addDataListener(new SerialPortMessageListenerWithExceptions {
      override def catchException(e: Exception): Unit = logger.error(s"comPort: $comPort", e)

      override def getMessageDelimiter = Array('\n')

      override def delimiterIndicatesEndOfMessage() = true

      override def getListeningEvents = LISTENING_EVENT_DATA_RECEIVED

      override def serialEvent(event: SerialPortEvent): Unit = {
        val receivedData: Array[Byte] = event.getReceivedData
        val response = new String(receivedData).trim

        response match {
          case "Complete" =>
            promise.success(builder.result())
            logger.debug("Complete")
          case "+SENDE" =>
            serialPort.closePort()
            logger.debug("+SENDE")
          case "EEPROM Done" =>
            write("OK")
            logger.debug("EEPROM Done")
          case "Timeout" =>
            logger.error(response)
            serialPort.closePort()
            promise.failure(new Exception(response))
          case x =>
            try {
              count += 1
              val parser(sIndex, value) = response
              val int = value.toInt
              logger.trace(s"$x -> $int")
              logger.whenDebugEnabled {
                if (count % 25 == 0)
                  logger.debug(s"$count")
              }
              builder += int
            } catch {
              case e: Exception =>
                logger.error(s"response: $response", e)
            }
            write("OK")
        }
      }
    }
    )
    val bytesAvailable = serialPort.bytesAvailable()
    if (bytesAvailable > 0) {
      val drain: Array[Byte] = new Array[Byte](bytesAvailable)
      serialPort.readBytes(drain, bytesAvailable)
      logger.info(s"drained: $bytesAvailable bytes. $drain")
    }
    write("1SendEram")
    promise.future
  }
}

//  new SerialPortMessageListenerWithExceptions
//  private val promise: Promise[Seq[Int]] =  Promise[Seq[Int]]()
//  private
//  override def catchException(e: Exception): Unit = {
//    logger.error(s"$comPort", e)
//  }
//
//  override def getMessageDelimiter: Array[Byte] = Array('\n')
//
//  override def delimiterIndicatesEndOfMessage(): Boolean = true
//
//  override def getListeningEvents: Int = LISTENING_EVENT_DATA_RECEIVED
//
//  override def serialEvent(event: SerialPortEvent): Unit = {
//    val receivedData: Array[Byte] = event.getReceivedData
//    val response = new String(receivedData).trim
//
//    // save data
//    //  ack with OK
//
//  }
//
//  def download:Future[Seq[Int]] = {
//    val serialPort: SerialPort = SerialPort.getCommPort(comPort.descriptor)
//
//    serialPort.setBaudRate(19200)
//    val opened: Boolean = serialPort.openPort()
//    if (!opened) {
//      throw new IOException(s"Did not open $comPort")
//    }
//    serialPort.addDataListener(this)
//    val bytes = "1SendEram\r\n".getBytes
//    serialPort.writeBytes(bytes, bytes.length)
//
//   promise.future
//  }
//}
