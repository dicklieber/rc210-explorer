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

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Row}
import net.wa9nnn.rc210.serial.ComPortPersistence

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Try, Using}

/**
 * This is the primary API for RC210 access, except for download.
 *
 * @param comPortPersistence whre to find the currently selected [[net.wa9nnn.rc210.serial.ComPort]].
 */
@Singleton
class Rc210 @Inject()(comPortPersistence: ComPortPersistence) extends LazyLogging {

  def send(name: String, requests: String *): Try[BatchOperationsResult] = {
    comPortPersistence.currentComPort match {
      case None =>
        Failure(new IllegalStateException("No Serial Port Selected."))
      case Some(comPort) =>
        Using(RcOperation(comPort)) { rcOperation =>
          BatchOperationsResult(name, requests.map { request =>
            rcOperation.sendOne(request)
          })
        }
    }
  }
}

case class BatchOperationsResult(name: String, results: Seq[RcOperationResult]) {
  def toRows: Seq[Row] = {
    val topRow: Row = results.head.toRow()
    val withNameColl: Row = topRow.copy(cells =
      topRow.cells.prepended(
        Cell(name)
          .withRowSpan(results.size)))
    val moreRows: Seq[Row] = results.tail.map(_.toRow())

    val rows: Seq[Row] = withNameColl +: moreRows
    rows
  }

}