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
import net.wa9nnn.rc210.serial.comm.RcEventBased
import net.wa9nnn.rc210.util.Configs

import java.io.PrintWriter
import java.nio.file.{Files, Path}
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}

/**
 * Reads eeprom from RC210 using the "1SendEram" command
 * Records it's status in _maybeLastDownload; accessable via [[downloadState]].
 * Saves data as a [[Memory]] recorded in [[memoryFile]].
 * Wen finished asks the [[DataStore]] to reload the [[Memory]] file.
 */
@Singleton
class DataCollector @Inject()(implicit config: Config, rc210: Rc210, dataStore: DataStore) extends LazyLogging:
  var stopAfter: Int = Integer.MAX_VALUE
  logger.whenTraceEnabled{
    stopAfter = 100
  }

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
    val operations = Seq.newBuilder[DownloadOp]
    val opCount = new AtomicInteger()

    val rcOperation: RcEventBased = rc210.openEventBased()
    val temMemoryFileWriter = new PrintWriter(Files.newOutputStream(tempFile))
    temMemoryFileWriter.println(s"stamp: ${Instant.now()}")

    def cleanup(error: String = ""): Unit =
      temMemoryFileWriter.close()
      _downloadState = _downloadState.complete(operations.result())
      rcOperation.close()
      if (error.isBlank)
        dataStore.reload()

    rcOperation.addDataListener(new SerialPortMessageListenerWithExceptions {
      // These overriden methods are the asynchronous callbacks invoked by jSerialComm.
      override def catchException(e: Exception): Unit = logger.error(s"comPort: ${rcOperation.comPort}", e)

      override def getMessageDelimiter: Array[Byte] = Array('\n')

      override def delimiterIndicatesEndOfMessage() = true

      override def getListeningEvents: Int = SerialPort.LISTENING_EVENT_DATA_RECEIVED

      override def serialEvent(event: SerialPortEvent): Unit = {
        val receivedData: Array[Byte] = event.getReceivedData
        val response = new String(receivedData).trim

        response match {
          // Handle the vaarious responses from the RC-210.
          case "Complete" =>
            cleanup()
            Files.deleteIfExists(memoryFile)
            Files.move(tempFile, memoryFile)
            progressApi.finish()
            logger.debug("Done")
          case "+SENDE" =>
            cleanup()
            logger.debug("+SENDE")
          case "EEPROM Done" =>
            rcOperation.send("OK")
            logger.debug("EEPROM Done")
            progressApi.finish()
          case "Timeout" =>
            progressApi.fatalError(Timeout(rcOperation.comPort))
            cleanup("timeout")
          case response =>
            try {
              val tokens: Array[String] = response.split(',')
              val line = f"${tokens.head.toInt}%04d:${tokens(1).toInt}"
              temMemoryFileWriter.println(line)
              
              progressApi.doOne(DownloadOp(line))
            } catch {
              case e: Exception =>
                logger.error(s"response: $response", e)
            }
            rcOperation.send("OK")
            if(opCount.incrementAndGet() >  stopAfter)
              cleanup("StopAfter")

        }
        operations.addOne(DownloadOp(response))
      }
    })
    rcOperation.send("1SendEram")

