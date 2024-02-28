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
 * Sends Comands tp RC-210 in a batch with Prigress UI.
 *
 * @param dataStore
 * @param rc210
 * @param config
 * @param mat
 */
class CommandsSender @Inject()(dataStore: DataStore, rc210: Rc210)
                              (implicit config: Config, mat: Materializer)
  extends LazyLogging {

  /**
   * Send commands for fields
   * This needs to run in a brackground thread; @see [[ProcessWithProgress]] @param sendField   what to send
   *
   * Results will live in [[LastSendBatch]]
   *
   * @param sendField   what to send for. all of just candiates.
   * @param field       None to send all fields. Of just the one.
   * @param progressApi where to report whats going on.
   */
  def apply(commandSendRequest: CommandSendRequest, progressApi: ProgressApi[RcResponse]): Unit = {
    val start = Instant.now()
    val streamBased: RcStreamBased = rc210.openStreamBased

    streamBased.perform(init)
    val fieldEntries: Seq[FieldEntry] = commandSendRequest.sendField match
      case SendField.AllFields =>
        dataStore.all
      case SendField.CandidatesOnly =>
        dataStore.candidates
      case SendField.TestVCandidates =>
        throw new NotImplementedError() //todo

    progressApi.expectedCount(fieldEntries.length)

    var errorEncountered = false
    for {
      fieldEntry <- fieldEntries
      fieldValue = fieldEntry.value.asInstanceOf[FieldValue]
      rcOperationResult <- streamBased.perform(fieldValue.toCommands(fieldEntry))
    } yield {
      progressApi.doOne(rcOperationResult)
    }
    progressApi.finish()
  }

}

object CommandsSender:
  val init: Seq[String] = Seq(
    "\r\r1333444555",
    "1*20990",
    "1GetVersion",
    "1GetRTCVersion",
  )

