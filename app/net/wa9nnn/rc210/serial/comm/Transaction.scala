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

package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse

import java.time.{Duration, Instant}
import scala.collection.mutable
import scala.concurrent.{Future, Promise}

case class Transaction(request: String, serialPort: SerialPort, start: Instant = Instant.now()) {
  private val lineBuilder: mutable.Builder[String, Seq[String]] = Seq.newBuilder[String]

  /**
   *
   * @param response one line from RC210.
   * @return true if the promise was completed.
   */
  def collect(response: String): Boolean = {
    lineBuilder += response
    response.head match {
      case '+' =>
        promise.success(lineBuilder.result())
        true
      case '-' =>
        promise.success(lineBuilder.result())
        true
      case x =>
        false
    }
  }

  private val promise = Promise[Seq[String]]()
  val future: Future[RcResponse] = promise.future
  private val bytes: Array[Byte] = request.getBytes
  serialPort.writeBytes(bytes, bytes.length)

  override def toString: String = {
    s"On: ${serialPort.getDescriptivePortName} Request: $request Duration: ${Duration.between(start, Instant.now())}"
  }
}