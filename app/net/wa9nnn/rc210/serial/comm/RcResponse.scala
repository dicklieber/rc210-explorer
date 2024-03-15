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
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.serial.ProgressItem
import net.wa9nnn.rc210.serial.comm.RealStreamBased.terminalPrefaces
import net.wa9nnn.rc210.util.expandControlChars

import scala.util.parsing.input.NoPosition.line
import scala.util.{Failure, Try}

case class RcResponse(in: String, lines: Seq[String]) extends ProgressItem with LazyLogging:
  val ok: Boolean = lines.nonEmpty && RealStreamBased.isTerminal(lines.last)
  lazy val sOk = if (ok) "ok" else "failed"
  logger.whenTraceEnabled {
    val sLines: String = lines.map(expandControlChars).mkString(" ")
    val value = s"in: ${expandControlChars(in)} response: $sLines $sOk"
    logger.trace(value)
  }

  def head: String = lines.headOption.getOrElse("empty")

  def responeLines: String = lines.mkString(" ")

  def detailCells: Seq[Cell] =
    val cssClass = if(ok)
      "happyCell"
    else
      "sadCell"
    Seq(
    Cell(expandControlChars(in)), 
    Cell(responeLines)
      .withCssClass(cssClass)
    )

  def toRow: Row =
    Row.ofAny(in, responeLines)

  def buildResultTable(items: Seq[ProgressItem]): Table =
    throw new NotImplementedError() //todo



