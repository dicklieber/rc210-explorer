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

import com.wa9nnn.wa9nnnutil.tableui.Table
import play.api.libs.json.{Format, Json}
import play.api.mvc.WebSocket.MessageFlowTransformer

import java.time.Instant
import scala.concurrent.duration.Duration

/**
 * Update progress that will go to the client.
 */
trait ProgressApi[T <: ProgressItem]:
  def expectedCount(count: Int): Unit

  /**
   * We processed one thing.
   *
   * @param message
   */
  def doOne(progressItem: T): Unit

  /**
   * We're done.
   *
   * @param message
   */
  def finish(): Unit

  /**
   * Something went wrong. Implies finish.
   *
   * @param exception
   */
  def fatalError(exception: Throwable): Unit

  def results: Seq[T]

/**
 * A DTO that is sent as JSON to client for progress display.
 *
 * @param percent    drives the progress bar.
 * @param resultHtml empty when running, HTML for the result <div> when finshed.
 */
case class Progress(percent: String = "", resultHtml: Option[String] = None)

object Progress:
  implicit val fmtProgress: Format[Progress] = Json.format[Progress]
  implicit val messageFlowTransformer: MessageFlowTransformer[String, Progress] =
    MessageFlowTransformer.jsonMessageFlowTransformer[String, Progress]