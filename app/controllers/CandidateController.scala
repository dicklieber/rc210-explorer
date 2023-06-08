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
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import configs.syntax._
import controllers.CandidateController.LastSendAll
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStoreActor._
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import net.wa9nnn.rc210.serial.{CommandTransaction, RC210IO, SerialPortOpenException, SerialPortOperation}
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
                          actor: ActorRef[Message],
                          rc210IO: RC210IO)
                         (implicit scheduler: Scheduler, ec: ExecutionContext, mat: Materializer)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private val sendFile: Path = config.get[Path]("vizRc210.sendLog").value
  private val stopOnError: Boolean = config.getBoolean("vizRc210.stopSendOnError")

  def index(): Action[AnyContent] = Action.async { implicit request =>
    actor.ask(Candidates).map { candidates =>
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
    actor.ask(All).map { entries =>
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
   * Send command to the RC-210.
   *
   * @param fieldKey  to send
   * @param sendValue true to send the fieldValue's command. false to send and accept the candidate's.
   * @return
   */
  def send(fieldKey: FieldKey, sendValue: Boolean = false): Action[AnyContent] = Action.async {
    implicit request =>
      actor.ask(ForFieldKey(fieldKey, _)).map { mayFieldEntry =>
        val fieldEntry: FieldEntry = mayFieldEntry.get // throws on failure.
        val fieldValue = if (sendValue)
          fieldEntry.fieldValue
        else
          fieldEntry.candidate.get // throws if no candidate.

        Using(rc210IO.start()) { serialPortOperation: SerialPortOperation =>
          for {
            command <- fieldValue.toCommands(fieldEntry)
          } yield {
            val withCr = "\r" + command + "\r"
            val transaction: CommandTransaction = CommandTransaction(0, withCr, fieldEntry.fieldKey.toCell, serialPortOperation.preform(withCr))
            if (!sendValue && transaction.isSuccess) {
              actor ! AcceptCandidate(fieldKey, who(request))
            }
            transaction.toRow
          }
        } match {
          case Failure(exception) =>
            exception match {
              case e: SerialPortOpenException =>
                InternalServerError(e.getMessage)
              case e: Throwable =>
                logger.error(s"Sending $fieldKey", e)
                InternalServerError(e.getMessage)
            }
          case Success(rows: Seq[Row]) =>
            val table = Table(Header("Result", "Field", "Command", "Response"), rows)
            Ok(views.html.juatdat(Seq(table)))
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
      val runnable = new Runnable {
        override def run(): Unit = {
          val start = Instant.now()
          val sendIndex = new AtomicInteger()
          val expectedCount = 461.0
          val sendLogWriter = new PrintWriter(Files.newBufferedWriter(sendFile))
          val sofar = new AtomicDouble()
          var maybeSerialPortOption: Option[SerialPortOperation] = None
          try {
            val serialPortOperation = rc210IO.start()
            maybeSerialPortOption = Option(serialPortOperation)
            val initRows: Seq[CommandTransaction] = performInit(serialPortOperation)
            val fieldEntries: Seq[FieldEntry] = Await.result[Seq[FieldEntry]](actor.ask(All), 5 seconds)
            val transactions: Seq[CommandTransaction] = for {
              fieldEntry <- fieldEntries // .take(25)
              command <- fieldEntry.fieldValue.toCommands(fieldEntry)
            } yield {
              val withCr = "\r" + command + "\r"
              val triedResponse: Try[String] = serialPortOperation.preform(withCr)
              val transaction = CommandTransaction(sendIndex.incrementAndGet(), withCr, fieldEntry.fieldKey.toCell, triedResponse)
              sendLogWriter.println(transaction.toString)
              sendLogWriter.flush()
              if (transaction.isFailure) {
                logger.error(transaction.toString)
                if (stopOnError)
                  throw new IllegalStateException("Stoping on  error.")
              }
              val percent = 100.0 * sofar.getAndAdd(1.0) / expectedCount
              val sPercent = f"$percent%.1f"
              queue.offer(sPercent)
              transaction
            }
            maybeLastSendAll = Option(LastSendAll(initRows :++ transactions, start))
          } catch {
            case e: Exception =>
              logger.error("WS Operation", e)
          } finally {
            maybeSerialPortOption.foreach(_.close())
            sendLogWriter.close()
          }
          queue.offer("Kinder das ist Alles")
        }
      }
      val thread: Thread = new Thread(runnable, "Download")
      thread.setDaemon(true)
      thread.start()
    }


    Flow.fromSinkAndSource(in, source)
  }

  def lastSendAll(): Action[AnyContent] = Action { implicit request =>
    maybeLastSendAll match {
      case Some(lastSendAll: LastSendAll) =>
        val rows = lastSendAll.transactions.map(_.toRow)
        val table = Table(CommandTransaction.header(s"All Fields (${rows.length})"), rows)
        Ok(views.html.lastSendAll(table, Option(lastSendAll)))
      case None =>
        Ok(views.html.lastSendAll(Table(Seq.empty, Seq.empty), None))
    }
  }
}

object CandidateController {
  private val init = Seq(
    "\r\r1333444555",
    "1*20990",
    "1GetVersion",
    "1GetRTCVersion",
  )

  def performInit(serialPortOperation: SerialPortOperation): Seq[CommandTransaction] = {
    for {
      command <- init
    } yield {
      Thread.sleep(125)
      val withCr = command + "\r"
      val triedResponse: Try[String] = serialPortOperation.preform(withCr)
      val transaction = CommandTransaction(0, withCr, Cell("Init"), triedResponse)
      transaction
    }
  }
}

case class LastSendAll(transactions: Seq[CommandTransaction], start: Instant, finish: Instant = Instant.now()) {
  val duration: Duration = Duration.between(start, finish)

  val successCount: Int = transactions.count(_.isSuccess)
  val failCount: Int = transactions.count(!_.isSuccess)

}