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
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import org.apache.pekko.stream.{Materializer, OverflowStrategy}
import org.apache.pekko.{Done, NotUsed}
import play.api.mvc.{RequestHeader, WebSocket}

import java.io.PrintWriter
import java.nio.file.{Files, Path}
import java.time.{Instant, LocalTime}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Future
import scala.language.implicitConversions
/**
 * Run a long running process that can report it's progress using the [[ProgressApi]]
 * This runs the process in a new Thread and handles developer logging and send [[Progress]] message to the client.
 *
 * @param mod           send progress every.
 * @param maybeSendfile where to log for dev debugging.
 * @param callback      what to do to do. With ProgressApi for reporting status.
 * @param mat           needed to Akka streams use by Play WebSocket.
 */
class ProcessWithProgress(mod: Int, maybeSendfile: Option[Path])(callback: ProgressApi => Unit)(implicit mat: Materializer)
  extends Thread with Runnable with LazyLogging with ProgressApi {

  val (queue, source) = Source.queue[Progress](250, OverflowStrategy.dropHead).preMaterialize()

  private val began = Instant.now()
  private var running = true
  private val count = new AtomicInteger()
  private val sendLogWriter: Option[PrintWriter] = maybeSendfile.map { path => new PrintWriter(Files.newBufferedWriter(path)) }
  private var expected: Int = 0

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

  def doOne(message: String): Unit = {
    val soFar = count.incrementAndGet()
    sendLogWriter.foreach { printWriter =>
      printWriter.println(s"$soFar\t${LocalTime.now()}\t $message")
      printWriter.flush()
    }
    sendProgress()
  }

  private def sendProgress(): Unit = {
    val soFar = count.get()
    if (!running || soFar % mod == 0) {
      val double = soFar * 100.0 / expected
      val progress = new Progress(
        running = running,
        soFar = soFar,
        percent = f"$double%2.1f%%",
        sDuration = DurationHelpers.between(began),
        expected = expected,
        start = began,
        error = ""
      )
      queue.offer(progress)
    }
  }

  def finish(message: String): Unit = {
    assert(running, "Must be running to finish!")
    running = false
    sendLogWriter.foreach(writer => writer.close())
    doOne(message)
    sendProgress()

  }

  override def error(exception: Throwable): Unit = {
    finish(exception.getMessage)
    sendProgress()
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
}
