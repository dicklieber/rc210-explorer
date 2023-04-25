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
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.data.field.FieldEntry

import scala.util.{Failure, Success, Try}

case class CommandTransaction(command: String, fieldEntry: FieldEntry, response: Try[String]) extends Stamped with RowSource {
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
    s"field: ${fieldEntry.fieldKey.param} $fixedCommand => $fixedResponse"
  }

  override def toRow: Row = {
    response match {
      case Failure(exception) =>
        Row(fieldEntry.fieldKey.toCell, fixUp(command), exception.getMessage).withCssClass("sadCell")
      case Success(response) =>
        val row = Row(fieldEntry.fieldKey.toCell, fixUp(command), fixUp(response))
        response.head match {
          case '+' =>
            row
          case x =>
            row.withCssClass("sadCell")
        }

    }
  }
}

object CommandTransaction {
  def header(topLine: String): Header = Header(topLine, "Field", "Command", "Response")
}