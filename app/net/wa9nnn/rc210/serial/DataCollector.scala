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
import com.wa9nnn.wa9nnnutil.tableui.Table
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.serial
import net.wa9nnn.rc210.serial.comm.*
import net.wa9nnn.rc210.util.Configs

import java.io.PrintWriter
import java.nio.file.{Files, Path}
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.util.Try
import scala.util.parsing.input.NoPosition.line

/**
 * Reads eeprom from RC210 using the "1SendEram" command
 * Records it's status in _maybeLastDownload; accessable via [[downloadState]].
 * Saves data as a [[Memory]] recorded in [[memoryFile]].
 * Wen finished asks the [[DataStore]] to reload the [[Memory]] file.
 */
@Singleton
class DataCollector @Inject()(implicit config: Config, rc210: Rc210, dataStore: DataStore) extends LazyLogging:

  val memoryFile: Path = Configs.path("vizRc210.memoryFile")
  val tempFile: Path = memoryFile.resolveSibling(memoryFile.toFile.toString + ".temp")
  val expectedLines: Int = config.getInt("vizRc210.expectedRcLines")
  val progressMod: Int = config.getInt("vizRc210.showProgressEvery")
  private var _downloadState: DownloadState = serial.DownloadState.neverStarted

  def downloadState: DownloadState = _downloadState

  /**
   *
   * @param requestTable what user requested.
   */
  def newDownload(requestTable: Table): Unit =
    _downloadState = _downloadState.start(requestTable)

  /**
   *
   * @return a Future that will be completed with the final [[Progress]] when done and a [[sun.net.ProgressSource]] that can be used to obtain the current [[Progress]] while download is running.
   */
  def startDownload(progressApi: ProgressApi[DownloadOp]): Unit =
    progressApi.expectedCount(expectedLines)

    val eventBased: EventBased = rc210.openEventBased()
    val itemMemoryFileWriter = new PrintWriter(Files.newOutputStream(tempFile))
    itemMemoryFileWriter.println(s"stamp: ${Instant.now()}")

    def cleanup(error: String = ""): Unit =
      itemMemoryFileWriter.close()
      eventBased.close()
      if (error.isBlank)
        dataStore.load()

    eventBased.addDataListener(new SerialPortMessageListenerWithExceptions {
      // These overriden methods are the asynchronous callbacks invoked by jSerialComm.
      override def catchException(e: Exception): Unit = logger.error(s"comPort: ${eventBased.toString}", e)

      override def getMessageDelimiter: Array[Byte] = Array('\n')

      override def delimiterIndicatesEndOfMessage() = true

      override def getListeningEvents: Int = SerialPort.LISTENING_EVENT_DATA_RECEIVED

      override def serialEvent(event: SerialPortEvent): Unit = {
        val receivedData: Array[Byte] = event.getReceivedData
        val response = new String(receivedData).trim

        response match {
          // Handle the various responses from the RC-210.
          case "Complete" =>
            cleanup()
            Files.deleteIfExists(memoryFile)
            Files.move(tempFile, memoryFile)
            progressApi.finish()
            logger.trace("\tDone")

          case m@"+SENDE" =>
            cleanup()
            logger.debug("\t{}}", m)
          case "EEPROM Done" =>
            eventBased.send("OK")
            logger.debug("\tEEPROM Done")
            progressApi.finish()
          case "Timeout" =>
            progressApi.fatalError(Timeout(eventBased.toString))
            cleanup("timeout")
          case response =>
            val tokens: Array[String] = response.split(',')
            if(tokens.length != 2)
              {
                logger.error(s"""Illformed response from RC-210, expecting something like "xxx,yyy" but got: "$response" """)
              }
            val memoryFileLine: String = f"${tokens.head.toInt},${tokens(1).toInt}"
            itemMemoryFileWriter.println(memoryFileLine)
            val downloadOp = DownloadOp(response)
            progressApi.doOne(downloadOp)
            eventBased.send("OK")
        }
      }
    })
    eventBased.send("\r\r\r1SendEram")

