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

package net.wa9nnn.rc210.data.clock

import play.api.data.Forms.*
import play.api.data.Mapping
import play.api.libs.json.{Format, Json}

case class DSTPoint(monthOfYearDST: MonthOfYearDST, occurrence: Occurrence) {
  def commandPiece: String = f"${monthOfYearDST.rc210Value}%02d${occurrence.rc210Value}"
}

object DSTPoint:
  val dstPointForm: Mapping[DSTPoint] =
    mapping(
      "month" -> MonthOfYearDST.formField,
      "occurrence" -> Occurrence.formField,
    )(DSTPoint.apply)(DSTPoint.unapply)

  def unapply(u: DSTPoint): Option[(MonthOfYearDST, Occurrence)] = Some((u.monthOfYearDST, u.occurrence))
  implicit val fmtDSTPoint: Format[DSTPoint] = Json.format[DSTPoint]

  def apply(s: String): DSTPoint =
    val month: MonthOfYearDST = {
      val i: Int = s.take(2).toInt
      MonthOfYearDST.values(i)
    }
    val occurance: Occurrence = {
      val i = s.takeRight(1).toInt - 1
      Occurrence.values(i)
    }
    new DSTPoint(month, occurance)

