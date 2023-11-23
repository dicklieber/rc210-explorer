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

package controllers

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, Table}
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.stream.Materializer
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import java.nio.file.Path
import java.time.{Duration, Instant}
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.Seq
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import org.apache.pekko.util.Timeout

import javax.inject.Inject



@Singleton()
class CandidateController @Inject()(dataStoreActor: ActorRef[DataStoreActor.Message],
                                    rc210: Rc210)
                                   (implicit config: Config, scheduler: Scheduler, ec: ExecutionContext, mat: Materializer)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private val sendLogFile: Path = path("vizRc210.sendLog")
  private val stopOnError: Boolean = config.getBoolean("vizRc210.stopSendOnError")

  private val init = Seq(
    "\r\r1333444555",
    "1*20990",
    "1GetVersion",
    "1GetRTCVersion",
  )

  def index(): Action[AnyContent] = Action.async { implicit request =>
    dataStoreActor.ask(Candidates.apply).map { candidates =>
      val rows: Seq[Row] =
        candidates.map { fieldEntry =>
          val candidate = fieldEntry.candidate.get
          Row(fieldEntry.fieldKey.key.toCell, fieldEntry.fieldKey.fieldName, fieldEntry.fieldValue.display, candidate.display, fieldEntry.toCommands)
        }
      val table = Table(Header(s"Candidates (${candidates.length})", "Key", "Field", "Was", "New", "Command"), rows)

      Ok(views.html.candidates(table))
    }
  }

  def dump(): Action[AnyContent] = Action.async {
    dataStoreActor.ask(All.apply).map { entries =>
      Ok(views.html.dump(entries))
    }
  }

  def sendFieldValue(fieldKey: FieldKey): Action[AnyContent] = {
    send(fieldKey, sendValue = true)
  }

  def sendCandidate(fieldKey: FieldKey): Action[AnyContent] = {
    send(fieldKey)
  }

  /**
   * Send command for one [[FieldKey]] to the RC210.
   *
   * @param fieldKey  to send
   * @param sendValue true to send the fieldValue's command. false to send and accept the candidate's.
   * @return
   */
  def send(fieldKey: FieldKey, sendValue: Boolean = false): Action[AnyContent] = Action.async {
    implicit request =>

      val eventualMaybeEntry: Future[Option[FieldEntry]] = dataStoreActor.ask(repTo => ForFieldKey(fieldKey, repTo))
      eventualMaybeEntry.map { (maybeFieldEntry: Option[FieldEntry]) =>
        maybeFieldEntry match
          case Some(fieldEntry) =>
            val commands: Seq[String] = if (sendValue)
              fieldEntry.value.toCommands(fieldEntry)
            else
              fieldEntry.candidate.get.toCommands(fieldEntry) // throws if no candidate

            val operationsResult: BatchOperationsResult = rc210.sendBatch(fieldKey.toString, commands: _*)
            val rows = operationsResult.toRows
            val table = Table(Header("Result", "Field", "Command", "Response"), rows)
            Ok(views.html.justdat(Seq(table)))
          case None =>
            NoContent
      }
  }

  def sendAllFields(): Action[AnyContent] = Action { implicit request =>
    val webSocketURL: String = routes.CandidateController.ws(471).webSocketURL() //todo all fields expected.
    Ok(views.html.progress(webSocketURL))
  }

  def sendAllCandidates(): Action[AnyContent] = Action { implicit request =>
    Ok("todo")
  }

  var maybeLastSendAll: Option[LastSendAll] = None

//  import play.api.mvc._

  def ws(expected: Int): WebSocket = {
    val start = Instant.now()
    ProcessWithProgress(expected, 7, Option(sendLogFile)) { (progressApi: ProgressApi) =>
      val streamBased: RcStreamBased = rc210.openStreamBased
      val operations = Seq.newBuilder[BatchOperationsResult]
      operations += streamBased.perform("Wakeup", init)
      val fieldEntries: Seq[FieldEntry] = Await.result[Seq[FieldEntry]](dataStoreActor.ask(All.apply), 5 seconds)

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
      maybeLastSendAll = Option(LastSendAll(operations.result(), start))
      progressApi.finish("Done")
    }
  }

  def lastSendAll(): Action[AnyContent] = Action { implicit request =>
    maybeLastSendAll match {
      case Some(lastSendAll: LastSendAll) =>
        val rows: Seq[Row] = lastSendAll.operations.flatMap(_.toRows)
        val table = Table(RcOperationResult.header(rows.length), rows)
        Ok(views.html.lastSendAll(table, Option(lastSendAll)))
      case None =>
        Ok(views.html.lastSendAll(Table(Seq.empty, Seq.empty), None))
    }
  }
}


case class LastSendAll(operations: Seq[BatchOperationsResult], start: Instant, finish: Instant = Instant.now()) {
  val duration: Duration = Duration.between(start, finish)
  var successCount: Int = 0
  var failCount: Int = 0

  for {
    bo: BatchOperationsResult <- operations
    op: RcOperationResult <- bo.results
  } {
    if (op.isSuccess)
      successCount += 1
    else
      failCount += 1
  }

}

