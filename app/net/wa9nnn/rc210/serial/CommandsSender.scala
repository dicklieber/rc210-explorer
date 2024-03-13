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
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table, TableInACell}
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.serial.CommandsSender.init
import net.wa9nnn.rc210.serial.comm.{RcResponse, RcStreamBased}
import org.apache.pekko.stream.Materializer

import java.time.Instant
import javax.inject.{Inject, Singleton}

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
  def newUpload(uploadRequest: UploadRequest): Unit =
    _uploadState = UploadState(uploadRequest)

  def startDownload(progressApi: ProgressApi[FieldResult]): Unit =
    val uploadDatas: Seq[UploadData] = _uploadState.uploadRequest.filter(dataStore)
    progressApi.expectedCount(uploadDatas.length)
    val streamBased: RcStreamBased = rc210.openStreamBased

    streamBased.perform(init)
    var errorEncountered = false

    uploadDatas.map { uploadData =>
      val fieldEntry = uploadData.fieldEntry
      val commands: Seq[String] = uploadData.fieldValue.toCommands(fieldEntry)
      val responses: Seq[RcResponse] = streamBased.perform(commands)
      val fieldResult = FieldResult(uploadData.fieldEntry.fieldKey, responses)
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

case class FieldResult(fieldKey: FieldKey, responses: Seq[RcResponse]) extends ProgressItem:
  val ok: Boolean =
    val errorCount: Int = responses.foldLeft(0)((accum, rcResponse) =>
      if (!rcResponse.ok)
        accum + 1
      else
        accum
    )
    errorCount == 0

  def toRow: Row = {
    val rows = responses.map(_.toRow)
    Row(
      fieldKey.display,
      TableInACell(Table(Header.none, rows))
    )
  }