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

import com.wa9nnn.util.tableui.{Cell, Header, Row, RowSource}
import net.wa9nnn.rc210.serial.comm.RcResponse

import scala.util.{Failure, Success, Try}
import scala.xml.Elem

case class RcOperationResult(request: String, triedResponse: Try[RcResponse]) {

  def isSuccess: Boolean = triedResponse.isSuccess

  def isFailure: Boolean = triedResponse.isFailure

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


  }

  object RcOperationResult {
    def header(count: Int): Header = Header(s"RC Operation Results ($count)", "Field", "Command", "Response")
  }
