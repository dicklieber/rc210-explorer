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

import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.serial.ComPort
import net.wa9nnn.rc210.serial.comm.DownloadStatus.expectedInts
import play.api.libs.json.{Format, Json}

import java.time.{Duration, Instant}


/**
 * Mutable state for [[net.wa9nnn.rc210.serial.ERamCollector]].
 */
class DownloadStatus(comPort: ComPort) {
  private val start: Instant = Instant.now()
  private var running: Boolean = true // false when finished.
  private var n: Int = 0

  def update(n: Int): Unit = {
    assert(running, "Can't update after finish!")
    this.n = n
  }

  def finish(): Unit = {
    assert(running, "Already finished!")
    running = false
  }

  def progress: Progress = {
    val double = n * 100.0 / expectedInts
    Progress(running, f"$double%2.1f%%")
  }

  def duration: Duration = Duration.between(start, Instant.now())
  def itemsPerSecond: Int = if (n > 0 && duration.getSeconds > 0)
    n / duration.getSeconds.toInt
  else
    0

  def toTable: Table = {
    val rows = Seq(
      Row("Descriptor", comPort.descriptor),
      Row("Friendly Name", comPort.friendlyName),
      Row("Items Loaded", n),
      Row("Started", start),
      Row("Duration", duration),
      Row("Items Per Second", itemsPerSecond)
    )
    Table(Header("RC-210 Download Result", "Item", "Value"), rows)
  }
}

object DownloadStatus {
  val expectedInts: Int = 4097 + 15 * 20 // main ints extended + macros * extendSlots.
}

case class Progress(running: Boolean, percent: String)

object Progress {
  implicit val fmtProgress: Format[Progress] = Json.format[Progress]
}