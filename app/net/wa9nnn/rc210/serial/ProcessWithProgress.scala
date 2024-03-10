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

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.DurationHelpers
import com.wa9nnn.wa9nnnutil.DurationHelpers.given
import com.wa9nnn.wa9nnnutil.tableui.{CellProvider, Header, Row, RowSource, Table}
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import org.apache.pekko.stream.{Materializer, OverflowStrategy}
import org.apache.pekko.{Done, NotUsed}
import play.api.mvc.{RequestHeader, WebSocket}
import play.twirl.api.Html
import views.html.downloadResult

import java.io.PrintWriter
import java.nio.file.{Files, Path}
import java.time.{Instant, LocalTime}
import java.util.concurrent.atomic.AtomicInteger
import scala.compiletime.ops.double
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

/**
 * Run a long running process that can report it's progress using the [[ProgressApi]]
 * This runs the process in a new Thread and handles developer logging and send [[Progress]] message to the client.
 *
 * @param mod                  send progress every.
 * @param inputTable           a table to show in the results.
 * @param callback             what to do. With ProgressApi for reporting status.
 * @param mat                  needed to Akka streams use by Play WebSocket.
 */
class ProcessWithProgress[T <: ProgressItem](mod: Int)(callback: ProgressApi[T] => Unit)(implicit mat: Materializer)
  extends Thread with Runnable with LazyLogging with ProgressApi[T]:

  val (queue: SourceQueueWithComplete[Progress], source: Source[Progress, NotUsed]) = Source.queue[Progress](250, OverflowStrategy.dropHead).preMaterialize()

  private val began = Instant.now()
  private var soFar = 0
  private var expected: Int = 0
  private var fatalError: Option[Throwable] = None
  /**
   * Contains HTML string to show to user when finished.
   */
  private val itemsBuilder = Seq.newBuilder[T]

  setDaemon(true)
  setName("RcProcess")
  start()

  override def run(): Unit = {
    try {
      callback(this)
    } catch {
      case e: Exception =>
        logger.error(s"Running RcProcess thread!", e)
    }
  }

  def doOne(progressItem: T) =
    itemsBuilder.addOne(progressItem)
    soFar += 1
    sendProgress()

  override def results: Seq[T] =
    itemsBuilder.result()

  private def sendProgress(): Unit = {
    if (soFar % mod == 0) {
      val progress = buildProgressMessage
      queue.offer(progress)
    }
  }

  private def buildProgressMessage: Progress = {
    val double = soFar * 100.0 / expected
    val percent = f"$double%2.0f%%"
    new Progress(percent = percent, 
      soFar = soFar,
      duration = DurationHelpers.between(began)
    ) 
  }

  def finish(): Unit = {
    soFar = expected // gets us to 100%
    val duration = DurationHelpers.between(began)
    var successCount = 0
    var errorCount = 0
    val items: Seq[T] = itemsBuilder.result()
    items
      .foreach { (progresssItem: T) =>
        if (progresssItem.success)
          successCount += 1
        else
          errorCount += 1
      }

    val detailTable: Table = Table(Header("Download Result", "Field", "Value"),
      Seq(Row("Duration", duration),
        Row("Success", successCount),
        Row("Errors", errorCount)
      )
    )
    val errorRows: Seq[Row] = (for {
      failedItem <- items.filterNot(_.success)
    } yield {
      failedItem.toRow
    })

    val maybeErrorTable: Option[Table] = Option.when(errorRows.nonEmpty) {
      Table(Header("Errors", "In", "Error"), errorRows)
    }
    val html: Html = downloadResult(detailTable, maybeErrorTable)
    val progress = new Progress("100%",
      soFar = soFar,
      duration = DurationHelpers.between(began),
      html.body)

    queue.offer(progress)
  }

  override def fatalError(exception: Throwable): Unit = {
    fatalError = Option(exception)
    sendProgress()
    finish()
  }

  lazy val webSocket: WebSocket = WebSocket.accept[String, Progress] { (request: RequestHeader) =>
    Flow.fromSinkAndSource(sink, source)
  }
  /**
   * Handle message from client. Don't really expect any.
   */
  val sink: Sink[String, Future[Done]] = Sink.foreach[String] { message =>
    logger.error(s"message: $message")
  }

  override def expectedCount(count: Int): Unit = expected = count

trait ProgressItem extends RowSource:
  val in: String
  val tried: Try[String]

  def success: Boolean = tried.isSuccess
