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

import com.wa9nnn.util.Stamped
import com.wa9nnn.util.tableui.{Cell, Header, Row, RowSource}

import scala.util.{Failure, Success, Try}

case class CommandTransaction(index:Int, command: String, field: Cell, response: Try[String]) extends Stamped with RowSource {
  private def fixUp(in: String): String =
    in.replace("\r", "\\r")
      .replace("\n", "\\n")

  override def toString: String = {
    val fixedCommand = fixUp(command)
    val fixedResponse = response match {
      case Failure(exception) =>
        exception.getMessage
      case Success(value) =>
        fixUp(value)
    }
    val startOfLine = if(isSuccess)
      "Success"
      else
      "Failure"
    s"$startOfLine:: Index:$index Field: ${field.value} $fixedCommand => $fixedResponse"
  }

  val isSuccess: Boolean = response.map(_.contains('+')).getOrElse(false)
  val isFailure: Boolean = !isSuccess

  override def toRow: Row = {
    response match {
      case Failure(exception) =>
        Row(field, fixUp(command), exception.getMessage).withCssClass("sadCell")
      case Success(response) =>
        val row = Row(Cell(index), field, fixUp(command), fixUp(response))
        row.withCssClass(if (isSuccess)
          "happyCell"
        else
          "sadCell"
        )
    }
  }
}

object CommandTransaction {
  def header(topLine: String): Header = Header(topLine, "Index", "Field", "Command", "Response")
}