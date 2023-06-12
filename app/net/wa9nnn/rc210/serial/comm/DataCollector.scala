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
import net.wa9nnn.rc210.data.datastore.DataStoreActor.RC210Result
import net.wa9nnn.rc210.serial.ComPort
import net.wa9nnn.rc210.serial.comm.DataCollector._
import play.api.libs.json.{Format, Json}

import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Future, Promise}
import scala.util.matching.Regex

/**
 * Download the full dump of RC210 eeprom and extended eeprom.
 *
 * @param comPort to connect to.
 */
class DataCollector(portDescriptor:String) extends SerialPortMessageListenerWithExceptions with LazyLogging {
  logger.trace(s"Starting: $portDescriptor")
  private val promise = Promise[RC210Result]()
  private val start = Instant.now
  private var count = new AtomicInteger()
  private val eeprom = Seq.newBuilder[Int]
  private val extended = Seq.newBuilder[Int]
  private var builder = eeprom
  private val serialPort = SerialPort.getCommPort(portDescriptor)
  serialPort.setBaudRate(19200)
  if(serialPort.openPort())
    logger.trace("{} opened", portDescriptor)

  serialPort.addDataListener(this)

  private def write(s: String = ""): Unit = {
    val bytes = (s + '\r').getBytes
    serialPort.writeBytes(bytes, bytes.length)
  }

  def future: Future[RC210Result] = promise.future
  // wakeup and maybe get to good state
  for (_ <- 0 to 3) {
    write()
    Thread.sleep(100)
  }

  private def switchToExtended(): Unit = builder = extended

  def progress: Progress = {
    val double = count.get() * 100.0 / expectedInts
    Progress(serialPort.isOpen, f"$double%2.1f%%", Duration.between(start, Instant.now()))
  }

  override def catchException(e: Exception): Unit = logger.error(s"comPort: $portDescriptor", e)

  override def getMessageDelimiter: Array[Byte] = Array('\n')

  override def delimiterIndicatesEndOfMessage() = true

  override def getListeningEvents: Int = SerialPort.LISTENING_EVENT_DATA_RECEIVED

  override def serialEvent(event: SerialPortEvent): Unit = {
    val receivedData: Array[Byte] = event.getReceivedData
    val response = new String(receivedData).trim

    response match {
      case "Complete" =>
        promise.success(RC210Result(eeprom.result(), extended.result(), progress))
        serialPort.closePort()
        logger.debug("Complete")
      case "+SENDE" =>
        serialPort.closePort()
        logger.debug("+SENDE")
      case "EEPROM Done" =>
        switchToExtended()
        write("OK")
        logger.debug("EEPROM Done")
      case "Timeout" =>
        logger.error(response)
        serialPort.closePort()
        promise.failure(new Exception(response))
      case x =>
        try {
          count.incrementAndGet()
          val parser(_, value) = response
          val int = value.toInt
          logger.trace(s"$x -> $int")
          logger.whenDebugEnabled {
            if (count.get() % 25 == 0)
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

  private val bytesAvailable = serialPort.bytesAvailable()
  if (bytesAvailable > 0) {
    val drain: Array[Byte] = new Array[Byte](bytesAvailable)
    serialPort.readBytes(drain, bytesAvailable)
    logger.info(s"drained: $bytesAvailable bytes: ${drain.map(_.toHexString).mkString(" ")}")
  }
  write("1SendEram")

}

object DataCollector {
  val parser: Regex = """(\d+),(\d+)""".r
  private val expectedInts: Int = 4097 + 15 * 20 // main ints extended + macros * extendSlots.
}


case class Progress(running: Boolean, percent: String, duration: String)

object Progress {
  implicit val fmtProgress: Format[Progress] = Json.format[Progress]
}

