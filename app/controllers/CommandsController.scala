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
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.datastore.DataStoreActor.*
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey, FieldValue}
import net.wa9nnn.rc210.security.authorzation.AuthFilter
import net.wa9nnn.rc210.serial.*
import net.wa9nnn.rc210.serial.comm.RcStreamBased
import net.wa9nnn.rc210.util.Configs.path
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.stream.Materializer
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import views.html.batchOpResult

import java.nio.file.Path
import java.time.{Duration, Instant}
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.Seq
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

/**
 * Sends commands to the RC-210.
 */
@Singleton()
class CommandsController @Inject()(dataStoreActor: ActorRef[DataStoreActor.Message],
                                   rc210: Rc210)
                                  (implicit config: Config, scheduler: Scheduler, ec: ExecutionContext, mat: Materializer, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private val sendLogFile: Path = path("vizRc210.sendLog")
  private val stopOnError: Boolean = config.getBoolean("vizRc210.stopSendOnError")

  private val init = Seq(
    "\r\r1333444555",
    "1*20990",
    "1GetVersion",
    "1GetRTCVersion",
  )

  /*  def index(): Action[AnyContent] = Action.async { implicit request =>
      dataStoreActor.ask(Candidates.apply).map { candidates =>
        Ok(views.html.candidates(candidates))
      }
    }*/

  def index: Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] => {
      val actorResult: Future[DataStoreReply] = dataStoreActor.ask(DataStoreMessage(Candidates, _))
      actorResult.map { (reply: DataStoreReply) => {
        reply.forAll { fieldEntries =>
          Ok(views.html.candidates(fieldEntries))
        }
      }
      }
    }
  }


  def dump(): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val actorResult: Future[DataStoreReply] = dataStoreActor.ask(DataStoreMessage(All, _))
      actorResult.map { dataStoreReply =>
        dataStoreReply.forAll { fieldEntries =>
          Ok(views.html.dump(fieldEntries))
        }
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
      val actorResult: Future[DataStoreReply] = dataStoreActor.ask(DataStoreMessage(ForFieldKey(fieldKey), _))

      actorResult.map { dataStoreReply =>
        dataStoreReply.forEntry { fieldEntry =>
          val commands: Seq[String] = fieldEntry
            .candidate
            .get
            .toCommands(fieldEntry)
          val batchOperationsResult: BatchOperationsResult = rc210.sendBatch(fieldKey.toString, commands: _*)
          Ok(batchOpResult(batchOperationsResult))
        }
      }
  }

  def sendFields(sendField: SendField): Action[AnyContent] = Action { implicit request =>
    val webSocketURL: String = routes.CommandsController.ws(sendField).webSocketURL() //todo all fields expected.
    Ok(views.html.progress(webSocketURL))
  }

  var maybeLastSendAll: Option[LastSendAll] = None


  def ws(sendField: SendField): WebSocket = {
    //todo handle authorization See https://www.playframework.com/documentation/3.0.x/ScalaWebSockets
    implicit request:Request[AnyContent] =>
    val start = Instant.now()
    ProcessWithProgress(7, Option(sendLogFile)) { (progressApi: ProgressApi) =>
      val streamBased: RcStreamBased = rc210.openStreamBased
      val operations = Seq.newBuilder[BatchOperationsResult]
      operations += streamBased.perform("Wakeup", init)
      val future: Future[DataStoreReply] = dataStoreActor.ask(DataStoreMessage( sendField.dataStoreRequest, _))
      val dataStoreReply: DataStoreReply = Await.result(future, 5 seconds)
      progressApi.expectedCount(dataStoreReply.length)

      var errorEncountered = false
      for {
        fieldEntry <- dataStoreReply.all
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
        Ok(views.html.lastSendAll(lastSendAll))
      case None =>
        NoContent

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

