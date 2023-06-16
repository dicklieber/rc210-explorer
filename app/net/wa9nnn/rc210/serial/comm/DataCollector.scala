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
import com.wa9nnn.util.TimeConverters.durationToString
import play.api.libs.json.{Format, Json}

import java.io.PrintWriter
import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Future, Promise}

/**
 * Reads eeprom from RC210 using the "1SendEram" command
 */
object DataCollector extends LazyLogging {
  /**
   *
   * @param portDescriptor serial portto connect to.
   * @param printWriter    where to send lines to. this will be closed at completion.
   * @return a Future that will be completed with the final [[Progress]] when done and a [[ProgressSource]] that can be used to obtain the current [[Progress]] while downloa is running.
   */
  def apply(printWriter: PrintWriter, portDescriptor: String): DataCollectorStuff = {
    logger.trace(s"Starting: $portDescriptor")
    val promise = Promise[Progress]()
    val start = Instant.now
    val count = new AtomicInteger()
    val serialPort = SerialPort.getCommPort(portDescriptor)
    serialPort.setBaudRate(19200)
    if (serialPort.openPort())
      logger.trace("{} opened", portDescriptor)


    def write(s: String = ""): Unit = {
      val bytes = (s + '\r').getBytes
      serialPort.writeBytes(bytes, bytes.length)
    }

    // wakeup and maybe get to good state
    for (_ <- 0 to 3) {
      write()
      Thread.sleep(100)
    }

    def progress: Progress = {
      val double = count.get() * 100.0 / expectedInts
      Progress(serialPort.isOpen, f"$double%2.1f%%", Duration.between(start, Instant.now()))
    }

    def cleanup(): Unit = {
      serialPort.closePort()
      printWriter.close()
      logger.debug("Complete")
    }

    serialPort.addDataListener(new SerialPortMessageListenerWithExceptions {

      override def catchException(e: Exception): Unit = logger.error(s"comPort: $portDescriptor", e)

      override def getMessageDelimiter: Array[Byte] = Array('\n')

      override def delimiterIndicatesEndOfMessage() = true

      override def getListeningEvents: Int = SerialPort.LISTENING_EVENT_DATA_RECEIVED

      override def serialEvent(event: SerialPortEvent): Unit = {
        val receivedData: Array[Byte] = event.getReceivedData
        val response = new String(receivedData).trim

        response match {
          case "Complete" =>
            cleanup()
            promise.success(progress)
          case "+SENDE" =>
            serialPort.closePort()
            logger.debug("+SENDE")
          case "EEPROM Done" =>
            write("OK")
            logger.debug("EEPROM Done")
          case "Timeout" =>
            logger.error(response)
            cleanup()
            promise.failure(new Exception(response))
          case response =>
            try {
              count.incrementAndGet()
              logger.whenDebugEnabled {
                if (count.get() % 25 == 0)
                  logger.debug(s"$count")
              }
              printWriter.println(response)
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
      logger.info(s"drained: $bytesAvailable bytes: ${drain.map(_.toHexString).mkString(" ")}")
    }

    write("1SendEram")


    val progressSource = new ProgressSource {
      override def apply(): Progress = progress
    }
    DataCollectorStuff(promise.future, progressSource)

  }

  //  val parser: Regex = """(\d+),(\d+)""".r
  private val expectedInts: Int = 4097 + 15 * 20 // main ints extended + macros * extendSlots.
}

case class DataCollectorStuff(future:Future[Progress], progressSource: ProgressSource)

case class Progress(running: Boolean = false, percent: String = "", duration: String = "")

object Progress {
  implicit val fmtProgress: Format[Progress] = Json.format[Progress]
}

trait ProgressSource {
  def apply(): Progress
}
