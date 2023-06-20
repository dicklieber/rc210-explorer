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

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import com.google.common.util.concurrent.AtomicDouble
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, Table}
import configs.syntax._
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor._
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.serial.ComPortPersistence
import net.wa9nnn.rc210.serial.comm.{OperationsResult, Rc210, RcOperation, RcOperationResult}
import play.api.mvc._

import java.io.PrintWriter
import java.nio.file.{Files, Path}
import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.Seq
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try, Using}

@Singleton()
class CandidateController @Inject()(config: Config,
                                    dataStoreActor: ActorRef[DataStoreActor.Message],
                                    rc210: Rc210,
                                    //                                    rcOperationsActor: ActorRef[RcOperationsActor.Message],
                                    comPortPersistence: ComPortPersistence)
                                   //                                    rcHelper: RcHelper)
                                   (implicit scheduler: Scheduler, ec: ExecutionContext, mat: Materializer)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private val sendFile: Path = config.get[Path]("vizRc210.sendLog").value
  private val stopOnError: Boolean = config.getBoolean("vizRc210.stopSendOnError")

  private val init = Seq(
    "\r\r1333444555",
    "1*20990",
    "1GetVersion",
    "1GetRTCVersion",
  )

  def index(): Action[AnyContent] = Action.async { implicit request =>
    dataStoreActor.ask(Candidates).map { candidates =>
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
    dataStoreActor.ask(All).map { entries =>
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

      dataStoreActor.ask(ForFieldKey(fieldKey, _)).map {
        maybeFieldEntry =>
          val fieldEntry: FieldEntry = maybeFieldEntry.get
          val commands: Seq[String] = if (sendValue)
            fieldEntry.value.toCommands(fieldEntry)
          else
            fieldEntry.candidate.get.toCommands(fieldEntry) // throws if no candidate

          val triedOperationsResult: Try[OperationsResult] = rc210.send(fieldKey.toString, commands: _*)
          triedOperationsResult match {
            case Failure(exception) =>
              InternalServerError(exception.getMessage)
            case Success(operationsResult: OperationsResult) =>
              val rows = operationsResult.toRows
              val table = Table(Header("Result", "Field", "Command", "Response"), rows)
              Ok(views.html.justdat(Seq(table)))

          }
      }
  }

  def sendAllFields(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.commandsProgress())
  }

  def sendAllCandidates(): Action[AnyContent] = Action { implicit request =>
    Ok("todo")
  }

  var maybeLastSendAll: Option[LastSendAll] = None

  import akka.stream.scaladsl._
  import play.api.mvc._

  def ws: WebSocket = WebSocket.accept[String, String] { request =>

    val (queue, source) = Source.queue[String](250, OverflowStrategy.dropHead).preMaterialize()

    // Log events to the console
    val in = Sink.foreach[String] { message =>
      logger.info("message: {}", message)
      val runnable: Runnable = new Runnable {
        override def run(): Unit = {
          val start = Instant.now()
          val sendIndex = new AtomicInteger()
          val expectedCount = 461.0
          val sendLogWriter = new PrintWriter(Files.newBufferedWriter(sendFile))
          val sofar = new AtomicDouble()

          comPortPersistence.currentComPort.foreach { comPort =>

            Using(RcOperation(comPort)) { rcOperation =>

              val operations = Seq.newBuilder[RcOperationResult]

              for (command <- init) {
                Thread.sleep(125)
                operations += rcOperation.perform(command)
              }

              val fieldEntries: Seq[FieldEntry] = Await.result[Seq[FieldEntry]](dataStoreActor.ask(All), 5 seconds)

              for {
                fieldEntry <- fieldEntries
                fieldValue = fieldEntry.value.asInstanceOf[FieldValue]
                command <- fieldValue.toCommands(fieldEntry)
              } yield {
                val rcOperationResult: RcOperationResult = rcOperation.perform(command)
                sendLogWriter.println(rcOperationResult.toString)
                sendLogWriter.flush()
                operations += rcOperationResult

                val percent = 100.0 * sofar.getAndAdd(1.0) / expectedCount
                val sPercent = f"$percent%.1f"
                queue.offer(sPercent)
              }

              maybeLastSendAll = Option(LastSendAll(operations.result(), start))
            }

            queue.offer("Kinder das ist Alles")
          }
        }

      }
      val thread: Thread = new Thread(runnable, "DownloadActor")
      thread.setDaemon(true)
      thread.start()
    }
    Flow.fromSinkAndSource(in, source)
  }

  def lastSendAll(): Action[AnyContent] = Action { implicit request =>
    maybeLastSendAll match {
      case Some(lastSendAll: LastSendAll) =>
        val rows: Seq[Row] = lastSendAll.operations.map(_.toRow())
        val table = Table(RcOperationResult.header(rows.length), rows)
        Ok(views.html.lastSendAll(table, Option(lastSendAll)))
      case None =>
        Ok(views.html.lastSendAll(Table(Seq.empty, Seq.empty), None))
    }
  }
}


case class LastSendAll(operations: Seq[RcOperationResult], start: Instant, finish: Instant = Instant.now()) {
  val duration: Duration = Duration.between(start, finish)

  val successCount: Int = operations.count(_.isSuccess)
  val failCount: Int = operations.count(!_.isFailure)

}