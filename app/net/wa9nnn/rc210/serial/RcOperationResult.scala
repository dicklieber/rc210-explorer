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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, RowSource}
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.serial.comm.RcResponse

import scala.util.{Failure, Success, Try}
import scala.xml.Elem

/**
 * 
 * @param request command to send to RC-210
 * @param triedResponse what we got back in response to sending the requeted c0mmand to the RC-210/
 */
case class RcOperationResult(request: String, triedResponse: Try[RcResponse]) extends ProgressItem {

  def success: Boolean = triedResponse.isSuccess

  def head: String = triedResponse match {
    case Failure(exception)
    =>
      exception.getMessage
    case Success(rcResponse: RcResponse)
    =>
      rcResponse.head
  }

  private def fixUp(in: String): String =
    in.replace("\r", "\\r")
      .replace("\n", "\\n")

  private def flatten(rcResponse: RcResponse): String = fixUp(rcResponse.lines.mkString(" "))

  override def toCell: Cell =
    triedResponse match
      case Failure(exception: Throwable) =>
        Cell(s"$request => ${exception.getMessage}")
          .withCssClass("happyCell")
      case Success(rcResponse: RcResponse) =>
        Cell(s"$request => ${rcResponse.head}")
          .withCssClass("happyCell")
}

object RcOperationResult {
  def header(count: Int): Header = Header(s"RC Operation Results ($count)", "Field", "Command", "Response")
}

case class FieldOperationsResult(fieldKey: FieldKey, results:Seq[RcOperationResult])
