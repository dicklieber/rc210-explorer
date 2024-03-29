/*
 * Copyright (C) 2024  Dick Lieber, WA9NNN
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

import com.wa9nnn.wa9nnnutil.DurationHelpers
import com.wa9nnn.wa9nnnutil.tableui.*

import java.time.Instant
import scala.collection.immutable.Seq
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

/**
 *
 * @param requestTable what user provided before running download.
 * @param start        when this began.
 * @param operations   each item downloaded. Empty before invoking complete.
 * @param finish       when this was complete.
 */
case class DownloadState(requestTable: Table, start: Instant = Instant.now(), operations: Seq[DownloadOp] = Seq.empty, finish: Instant = Instant.EPOCH):
  def start(requestTable: Table): DownloadState = new DownloadState(requestTable)

  def complete(operations: Seq[DownloadOp]): DownloadState = copy(operations = operations, finish = Instant.now)

  def summaryTable: Table =
    requestTable.append(KvTableSection("Result",
      "Finished" -> finish,
      "Duration" -> DurationHelpers.between(start, finish),
      "Items" -> operations.length
    ))

  def detailTable: Table =
    MultiColumn(operations, 10, "Detail")

object DownloadState:
  val neverStarted: DownloadState = new DownloadState(Table("Not Started", Seq.empty))

case class DownloadOp(response: String) extends ProgressItem:

  import net.wa9nnn.rc210.serial.DownloadOp.parser

  def toRow: Row = Row.ofAny(response)

  val in: String = "OK"
  val ok: Boolean = true

  def buildResultTable(items: Seq[ProgressItem]): Table =
    //    val detailTable: Table = Table(Header("Errors", "Field", "Value"),
    //      Seq(Row("Duration", duration),
    //        Row("Success", successCount),
    //        Row("Errors", errorCount)
    //      )
    //    )

    val errorRows: Seq[Row] = (for {
      failedItem <- items.filterNot(_.ok)
    } yield {
      failedItem.toRow
    })

    Table(Header("Errors", "In", "Error"), errorRows)

object DownloadOp:
  val parser: Regex = """(\d+),(\d+)""".r
