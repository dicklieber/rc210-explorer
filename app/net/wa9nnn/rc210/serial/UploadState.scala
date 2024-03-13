/*
 * Copyright (C) 2024  Dick Lieber, WA9NNN
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

import com.wa9nnn.wa9nnnutil.DurationHelpers
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.serial.comm.RcResponse

import java.time.Instant
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

/**
 *
 * @param uploadRequest what user provided before running download.
 * @param start        when this began.
 * @param operations   each item downloaded. Empty before invoking complete.
 * @param finish       when this was complete.
 */
case class UploadState(uploadRequest: UploadRequest,
                       rcSession:RcSession,
                       start: Instant = Instant.now(), 
                       operations: Seq[RcResponse] = Seq.empty, 
                       finish: Instant = Instant.EPOCH):
  def start(requestTable: Table): DownloadState = new DownloadState(requestTable)

  def complete(operations: Seq[RcResponse]): UploadState = copy(operations = operations, finish = Instant.now)


  def detailTable: Table =
    MultiColumn(operations, 10, "Detail")

object UploadState:
  val neverStarted: UploadState = new UploadState(UploadRequest(SendField.CandidatesOnly), rcSession = RcSession.noSession)


