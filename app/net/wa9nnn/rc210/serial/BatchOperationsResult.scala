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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row}

/**
 * Result for a named batch of RC210 operations under
 * @param name often a [[net.wa9nnn.rc210.data.FieldKey]]
 * @param results for each operation.
 */
case class BatchOperationsResult(name: String, results: Seq[RcOperationResult]) {
//  def toRows: Seq[Row] = {
//    val topRow: Row = results.head.toRow
//    val withNameColl: Row = topRow.copy(cells =
//      topRow.cells.prepended(
//        Cell(name)
//          .withRowSpan(results.size)))
//    val moreRows: Seq[Row] = results.tail.map(_.toRow)
//
//    val rows: Seq[Row] = withNameColl +: moreRows
//    rows
//  }
}