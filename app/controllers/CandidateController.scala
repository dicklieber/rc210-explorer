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

import akka.stream.{Materializer, OverflowStrategy}
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import controllers.CandidateController.performInit
import controllers.UpLoadProgress._
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.serial.{CommandTransaction, RC210IO, SerialPortOperation}
import play.api.libs.json.{Format, Json}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton()
class CandidateController @Inject()(dataStore: DataStore, rc210IO: RC210IO)(implicit mat: Materializer) extends MessagesInjectedController with LazyLogging {
  def index(): Action[AnyContent] = Action { implicit request =>
    val candidates = dataStore.candidates
    val rows: Seq[Row] =
      candidates.map { fieldEntry =>
        val candidate = fieldEntry.candidate.get
        Row(fieldEntry.fieldKey.key.toCell, fieldEntry.fieldKey.fieldName, fieldEntry.fieldValue.display, candidate.display, fieldEntry.toCommands)
      }
    val table = Table(Header(s"Candidates (${candidates.length})", "Key", "Field", "Was", "New", "Command"), rows)

    Ok(views.html.candidates(table))
  }

  def dump(): Action[AnyContent] = Action {
    Ok(views.html.dump(dataStore.all))
  }


  def sendFieldValue(fieldKey: FieldKey): Action[AnyContent] = {
    send(fieldKey, sendValue = true)
  }

  def sendCandidate(fieldKey: FieldKey): Action[AnyContent] = {
    send(fieldKey)
  }

  /**
   * Send command to the RC-210.
   *
   * @param fieldKey  to send
   * @param sendValue true to send the fieldValue's command. false to send and accept the candidate's.
   * @return
   */
  def send(fieldKey: FieldKey, sendValue: Boolean = false): Action[AnyContent] = Action { implicit request =>
    dataStore(fieldKey) match {
      case Some(fieldEntry: FieldEntry) =>

        val serialPortOperation = rc210IO.start()

        val commands: Seq[String] = fieldEntry.fieldValue.toCommands(fieldEntry)
        val rows: Seq[Row] = commands.map { command =>
          val withCr = "\r" + command + "\r"
          val triedResponse: Try[String] = serialPortOperation.preform(withCr)
          serialPortOperation.close()
          val transaction = CommandTransaction(withCr, fieldEntry.fieldKey.toCell, triedResponse)
          logger.debug(transaction.toString)
          transaction.toRow
        }
        val table = Table(CommandTransaction.header(s"All Fields (${rows.length})"), rows)
        Ok(views.html.dat(Seq(table)))
      case None =>
        NotFound(s"No fieldKey: $fieldKey")
    }
  }

  def sendAllFields(): Action[AnyContent] = Action { implicit request =>
    /*    val serialPortOperation = rc210IO.start()
        val initRows = performInit(serialPortOperation).map(_.toRow)
        val rows: Seq[Row] = for {
          fieldEntry <- dataStore.all.take(25)
          command <- fieldEntry.fieldValue.toCommands(fieldEntry)
        } yield {
          val withCr = "\r" + command + "\r"
          val triedResponse: Try[String] = serialPortOperation.preform(withCr)
          val transaction = CommandTransaction(withCr, fieldEntry.fieldKey.toCell, triedResponse)
          logger.debug(transaction.toString)
          queue.offer(transaction.toString)
          transaction.toRow
        }
        serialPortOperation.close()
     */
    //    val table = Table(CommandTransaction.header(s"All Fields (${rows.length})"), initRows ++ rows)
    Ok(views.html.commandsProgress())
  }

  def sendAllCandidates(): Action[AnyContent] = Action { implicit request =>
    Ok("todo")
  }


  import akka.stream.scaladsl._
  import play.api.mvc.WebSocket.MessageFlowTransformer
  import play.api.mvc._

  implicit val messageFlowTransformer: MessageFlowTransformer[String, UpLoadProgress] = MessageFlowTransformer.jsonMessageFlowTransformer[String, UpLoadProgress]

  def ws = WebSocket.accept[String, String] { request =>

    val (queue, source) = Source.queue[String](250, OverflowStrategy.dropHead).preMaterialize()

    var progress = UpLoadProgress(369)
    // Log events to the console
    val in = Sink.foreach[String] { message =>
      logger.info("message: {}", message)

      val serialPortOperation = rc210IO.start()
      val initRows = performInit(serialPortOperation).map(_.toRow)
      val rows: Seq[Row] = for {
        fieldEntry <- dataStore.all // .take(25)
        command <- fieldEntry.fieldValue.toCommands(fieldEntry)
      } yield {
        val withCr = "\r" + command + "\r"
        val triedResponse: Try[String] = serialPortOperation.preform(withCr)
        val transaction = CommandTransaction(withCr, fieldEntry.fieldKey.toCell, triedResponse)

        logger.debug(transaction.toString)
        progress = progress.add()
        val sProgressJson = Json.toJson(progress).toString()
        queue.offer(sProgressJson)
        logger.debug(sProgressJson)
        transaction.toRow
      }
      serialPortOperation.close()
      queue.offer("Kinder daqs ist Alles")
    }

    //    def sendProgress
    //
    //    queue.offer(progress)
    //    val serialPortOperation = rc210IO.start()
    //    val initRows = performInit(serialPortOperation).map(_.toRow)
    //    val rows: Seq[Row] = for {
    //      fieldEntry <- dataStore.all // .take(25)
    //      command <- fieldEntry.fieldValue.toCommands(fieldEntry)
    //    } yield {
    //      val withCr = "\r" + command + "\r"
    //      val triedResponse: Try[String] = serialPortOperation.preform(withCr)
    //      val transaction = CommandTransaction(withCr, fieldEntry.fieldKey.toCell, triedResponse)
    //
    //      logger.debug(transaction.toString)
    //      progress = progress.add()
    //      queue.offer(progress)
    //      transaction.toRow
    //    }
    //    serialPortOperation.close()
    //    val sender = new Sender()
    //    //    queue.offer(progress)
    //    val soource = sender.start
    Flow.fromSinkAndSource(in, source)
  }


  //  class Sender() extends Runnable {
  //    val (queue, source) = Source.queue[UpLoadProgress](250, OverflowStrategy.dropTail).preMaterialize()
  //    //    val runnableGraph: RunnableGraph[Source[String, NotUsed]] =
  //    //      source.toMat(BroadcastHub.sink(bufferSize = 256))(Keep.right)
  //
  //    var progress: UpLoadProgress = UpLoadProgress(300)
  //
  //    def start: Source[UpLoadProgress, NotUsed] = {
  //      new Thread(this).start()
  //      source
  //    }
  //
  //    override def run(): Unit = {
  //
  //      queue.offer(progress)
  //      val serialPortOperation = rc210IO.start()
  //      val initRows = performInit(serialPortOperation).map(_.toRow)
  //      val rows: Seq[Row] = for {
  //        fieldEntry <- dataStore.all // .take(25)
  //        command <- fieldEntry.fieldValue.toCommands(fieldEntry)
  //      } yield {
  //        val withCr = "\r" + command + "\r"
  //        val triedResponse: Try[String] = serialPortOperation.preform(withCr)
  //        val transaction = CommandTransaction(withCr, fieldEntry.fieldKey.toCell, triedResponse)
  //
  //        logger.debug(transaction.toString)
  //        progress = progress.add()
  //        queue.offer(progress)
  //        transaction.toRow
  //      }
  //      serialPortOperation.close()
  //
  //    }
  //  }
}


object CandidateController {
  private val init = Seq(
    "\r\r1333444555",
    "1*20990",
    "1GetVersion",
    "1GetRTCVersion",
  )

  def performInit(serialPortOperation: SerialPortOperation) = {
    for {
      command <- init
    } yield {
      Thread.sleep(125)
      val withCr = command + "\r"
      val triedResponse: Try[String] = serialPortOperation.preform(withCr)
      val transaction = CommandTransaction(withCr, Cell("Init"), triedResponse)
      transaction
    }
  }
}

case class UpLoadProgress(max: Int, soFar: Int = 0) {
  def add(): UpLoadProgress = {
    copy(soFar = soFar + 1)
  }
}

object UpLoadProgress {
  implicit val fmtFS: Format[FS] = Json.format[FS]
  implicit val fmtUpLoadProgress: Format[UpLoadProgress] = Json.format[UpLoadProgress]
}

case class FS(param: String = "", css: String = "", response: String = "none")


