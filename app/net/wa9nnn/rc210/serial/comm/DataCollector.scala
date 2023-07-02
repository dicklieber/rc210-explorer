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

import akka.actor.typed.ActorRef
import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import configs.syntax._
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.serial.{OpenedSerialPort, Timeout}

import java.io.PrintWriter
import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}

/**
 * Reads eeprom from RC210 using the "1SendEram" command
 */
@Singleton
class DataCollector @Inject()(config: Config, rc210: Rc210, dataStoreActor: ActorRef[DataStoreActor.Message]) extends LazyLogging {

  val memoryFile: Path = config.get[Path]("vizRc210.memoryFile").value
  val tempFile = memoryFile.resolveSibling(memoryFile.toFile.toString + ".temp")
  val expectedLines: Int = config.get[Int]("vizRc210.expectedRcLines").value

  /**
   *
   * @param progressBuilder    keep track of progress.
   * @param portDescriptor     serial port connect to.
   * @return a Future that will be completed with the final [[Progress]] when done and a [[ProgressSource]] that can be used to obtain the current [[Progress]] while download is running.
   */
  def apply(progressApi: ProgressApi): Unit = {
    try {
      doStuff()

    } catch {
      case e: Exception =>
        progressApi.error(e)

    } finally {

    }


    def doStuff(): Unit = {
      val openedSerialPort: OpenedSerialPort = rc210.openSerialPort

      val linesWriter = new PrintWriter(Files.newBufferedWriter(tempFile))

      def write(s: String = ""): Unit = {
        val bytes = (s + '\r')
        openedSerialPort.write(bytes)
      }

      // wakeup and maybe get to good state
      for (_ <- 0 to 3) {
        write()
        Thread.sleep(100)
      }

      def cleanup(): Unit = {
        openedSerialPort.close()
        dataStoreActor ! DataStoreActor.Reload()
        linesWriter.close()
        progressApi.finish("Complete")
      }

      openedSerialPort.addDataListener(new SerialPortMessageListenerWithExceptions {
        // serialPort sends us messages here.
        override def catchException(e: Exception): Unit = logger.error(s"comPort: $openedSerialPort", e)

        override def getMessageDelimiter: Array[Byte] = Array('\n')

        override def delimiterIndicatesEndOfMessage() = true

        override def getListeningEvents: Int = SerialPort.LISTENING_EVENT_DATA_RECEIVED

        override def serialEvent(event: SerialPortEvent): Unit = {
          val receivedData: Array[Byte] = event.getReceivedData
          val response = new String(receivedData).trim

          response match {
            case "Complete" =>
              cleanup()
              Files.delete(memoryFile)
              Files.move(tempFile, memoryFile)
              progressApi.finish("Done")

            case "+SENDE" =>
              openedSerialPort.close()
              logger.debug("+SENDE")
            case "EEPROM Done" =>
              write("OK")
              logger.debug("EEPROM Done")
            case "Timeout" =>
              progressApi.error(Timeout(openedSerialPort.toString))
              cleanup()
            case response =>
              try {
                linesWriter.println(response)
                progressApi.doOne(response)
              } catch {
                case e: Exception =>
                  logger.error(s"response: $response", e)
              }
              write("OK")
          }
        }
      }
      )

      val bytesAvailable = openedSerialPort.bytesAvailable()
      if (bytesAvailable > 0) {
        val drain: Array[Byte] = new Array[Byte](bytesAvailable)
        openedSerialPort.readBytes(drain, bytesAvailable)
        logger.info(s"drained: $bytesAvailable bytes: ${drain.map(_.toHexString).mkString(" ")}")
      }

      write("1SendEram") // start things off.
    }
  }
}
