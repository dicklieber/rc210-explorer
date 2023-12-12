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
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey, FieldValue}
import net.wa9nnn.rc210.security.authorzation.AuthFilter
import net.wa9nnn.rc210.serial.*
import net.wa9nnn.rc210.serial.comm.RcStreamBased
import net.wa9nnn.rc210.util.Configs.path
import org.apache.pekko.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.*
import views.html.batchOpResult

import java.nio.file.Path
import java.time.{Duration, Instant}
import javax.inject.{Inject, Singleton}

/**
 * Sends commands to the RC-210.
 */
@Singleton()
class CommandsController @Inject()(dataStore: DataStore,
                                   batchRc210Sender: BatchRc210Sender,
                                   rc210: Rc210)
                                  (implicit config: Config, mat: Materializer, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {
  private val sendLogFile: Path = path("vizRc210.sendLog")
  //  private val stopOnError: Boolean = config.getBoolean("vizRc210.stopSendOnError")

  /*  def index(): Action[AnyContent] = Action.async { implicit request =>
      dataStoreActor.ask(Candidates.apply).map { candidates =>
        Ok(views.html.candidates(candidates))
      }
    }*/

  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] => {
      val fieldEntries: Seq[FieldEntry] = dataStore.candidates
      Ok(views.html.candidates(fieldEntries))
    }
  }

  def dump(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldEntries = dataStore.candidates
      Ok(views.html.dump(fieldEntries))
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

def ws(sendField: SendField): WebSocket = WebSocket.accept[String, Progress] { implicit request =>
  //todo handle authorization See https://www.playframework.com/documentation/3.0.x/ScalaWebSockets
  val start = Instant.now()
  val p: ProcessWithProgress = ProcessWithProgress(7, Option(sendLogFile)) { progressApi =>
    batchRc210Sender(sendField, progressApi)
  }
  Flow.fromSinkAndSource(p.sink, p.source)
}

def lastSendAll(): Action[AnyContent] = Action { implicit request =>
  Ok(views.html.lastSendAll())
}

def socket: WebSocket = WebSocket.accept[String, String] { request =>
  // log the message to stdout and send response back to client
  Flow[String].map { msg =>
    println(msg)
    "I received your message: " + msg
  }
}

}

