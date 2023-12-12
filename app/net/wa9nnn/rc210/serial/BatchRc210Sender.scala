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
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.serial.BatchRc210Sender.init
import net.wa9nnn.rc210.serial.LastSend
import net.wa9nnn.rc210.serial.comm.RcStreamBased
//import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.*
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class BatchRc210Sender @Inject()(dataStore: DataStore, rc210: Rc210)
                                (implicit config: Config, mat: Materializer)
  extends LazyLogging {
  private val stopOnError: Boolean = config.getBoolean("vizRc210.stopSendOnError")

  /**
   *
   * @param sendField   what to send
   * @param progressApi where to report whats going on.
   */
  def apply(sendField: SendField, progressApi: ProgressApi): Unit = {
    val start = Instant.now()
    val streamBased: RcStreamBased = rc210.openStreamBased

    val operations = Seq.newBuilder[BatchOperationsResult]
    operations += streamBased.perform("Wakeup", init)
    val fieldEntries: Seq[FieldEntry] = sendField match
      case SendField.AllFields =>
        dataStore.all
      case SendField.CandidatesOnly =>
        dataStore.candidates
    progressApi.expectedCount(fieldEntries.length)

    var errorEncountered = false
    for {
      fieldEntry <- fieldEntries
      fieldValue = fieldEntry.value.asInstanceOf[FieldValue]
      if !(errorEncountered && stopOnError)
    } yield {
      val batchOperationsResult = streamBased.perform(fieldEntry.fieldKey.toString, fieldValue.toCommands(fieldEntry))
      batchOperationsResult.results.foreach { rcOperationResult =>
        errorEncountered = rcOperationResult.isFailure
        progressApi.doOne(rcOperationResult.toString)
      }
      operations += batchOperationsResult
    }
    LastSend.save(LastSendBatch(operations.result(), start))
    progressApi.finish("Done")
  }
}

object BatchRc210Sender:
  val init: Seq[String] = Seq(
    "\r\r1333444555",
    "1*20990",
    "1GetVersion",
    "1GetRTCVersion",
  )

end BatchRc210Sender
