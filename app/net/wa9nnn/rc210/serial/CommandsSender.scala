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

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.serial.CommandsSender.init
import net.wa9nnn.rc210.serial.comm.{RcResponse, RealStreamBased, StreamBased}
import org.apache.pekko.stream.Materializer

import javax.inject.*

@Singleton

/**
 * Sends Commands to RC-210 in a batch with Progress UI.
 *
 * @param dataStore
 * @param rc210
 * @param config
 * @param mat
 */
class CommandsSender @Inject()(dataStore: DataStore, rc210: Rc210)
                              (implicit config: Config, mat: Materializer)
  extends LazyLogging:
  private var _uploadState: UploadState = UploadState.neverStarted

  /**
   * Send commands for fields
   * This needs to run in a brackground thread; @see [[ProcessWithProgress]] @param sendField   what to send
   *
   * Results will live in [[LastSendBatch]]
   *
   * @param commandSendRequest   what user wants top upoad.
   */
  //  def newUpload(commandSendRequest: CommandSendRequest, progressApi: ProgressApi[RcResponse]): Unit = {
  def newUpload(uploadRequest: UploadRequest)(using rcSession:RcSession): Unit =
    _uploadState = UploadState(uploadRequest, rcSession)

  def startDownload(progressApi: ProgressApi[FieldResult]): Unit =
    given RcSession = _uploadState.rcSession
    val uploadDatas: Seq[UploadData] = _uploadState.uploadRequest.filter(dataStore)
    progressApi.expectedCount(uploadDatas.length)
    val streamBased: StreamBased = rc210.openStreamBased

    streamBased.perform(init)

    uploadDatas.foreach { uploadData =>
      val fieldEntry = uploadData.fieldEntry
      val commands: Seq[String] = fieldEntry.toCommands
      val responses: Seq[RcResponse] = streamBased.perform(commands)
      val key = uploadData.fieldEntry.key
      dataStore.acceptCandidate(key)
      val fieldResult = FieldResult(key, responses)
      progressApi.doOne(fieldResult)
    }
    progressApi.finish()

object CommandsSender:
  val init: Seq[String] = Seq(
    "\r\r1333444555",
    "1*20990",
    "1GetVersion",
    "1GetRTCVersion",
  )
