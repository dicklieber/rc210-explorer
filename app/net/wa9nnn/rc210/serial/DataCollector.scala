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

import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.serial.comm.RcEventBased
import org.apache.pekko.actor.typed.ActorRef

import java.io.PrintWriter
import java.nio.file.{Files, Path}
import java.time.Instant
import javax.inject.{Inject, Singleton}

/**
 * Reads eeprom from RC210 using the "1SendEram" command
 */
@Singleton
class DataCollector @Inject()(config: Config, rc210: Rc210, dataStoreActor: ActorRef[DataStoreActor.Message]) extends LazyLogging {

  val memoryFile: Path = config.get[Path]("vizRc210.memoryFile").value
  val tempFile: Path = memoryFile.resolveSibling(memoryFile.toFile.toString + ".temp")
  val expectedLines: Int = config.get[Int]("vizRc210.expectedRcLines").value
  val progreMod: Int = config.get[Int]("vizRc210.showProgressEvery").value

  /**
   *
   * @return a Future that will be completed with the final [[Progress]] when done and a [[sun.net.ProgressSource]] that can be used to obtain the current [[Progress]] while download is running.
   */
  def apply(progressApi: ProgressApi, maybeComment: Option[String]): Unit = {
    val rcOp: RcEventBased = rc210.openEventBased()
    val fileWriter = new PrintWriter(Files.newOutputStream(tempFile))
    fileWriter.println(s"stamp: ${Instant.now()}")
    maybeComment.foreach(comment => fileWriter.println(s"comment: $comment"))

    def cleanup(error: String = ""): Unit = {
      fileWriter.close()
      rcOp.close()
      if (error.isBlank)
        dataStoreActor ! DataStoreActor.Reload
    }


    rcOp.addDataListener(new SerialPortMessageListenerWithExceptions {

      override def catchException(e: Exception): Unit = logger.error(s"comPort: ${rcOp.comPort}", e)

      override def getMessageDelimiter: Array[Byte] = Array('\n')

      override def delimiterIndicatesEndOfMessage() = true

      override def getListeningEvents: Int = SerialPort.LISTENING_EVENT_DATA_RECEIVED

      override def serialEvent(event: SerialPortEvent): Unit = {
        val receivedData: Array[Byte] = event.getReceivedData
        val response = new String(receivedData).trim

        response match {
          case "Complete" =>

            cleanup()
            Files.deleteIfExists(memoryFile)
            Files.move(tempFile, memoryFile)
            progressApi.finish("Done")
            logger.debug("Done")
          case "+SENDE" =>
            cleanup()
            logger.debug("+SENDE")
          case "EEPROM Done" =>
            rcOp.send("OK")
            logger.debug("EEPROM Done")
          case "Timeout" =>
            progressApi.error(Timeout(rcOp.comPort))
            cleanup("timeout")
          case response =>
            try {
              val tokens: Array[String] = response.split(',')
              val line = f"${tokens.head.toInt}%04d:${tokens(1).toInt}"
              fileWriter.println(line)
              progressApi.doOne(line)
            } catch {
              case e: Exception =>
                logger.error(s"response: $response", e)
            }
            rcOp.send("OK")
        }
      }
    })
    rcOp.send("1SendEram")

  }


}





