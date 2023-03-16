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

package net.wa9nnn.rc210.data.schedules

import play.api.libs.json._

object MonthOfYear extends Enumeration {
  type MonthOfYear = Value
  val EveryMonth,
  January, February, March, April, May, June, July, August, September, October, November, December = Value

  implicit val fmtMonthOfYear: Format[MonthOfYear] = new Format[MonthOfYear] {
    override def reads(json: JsValue): JsResult[MonthOfYear] = {
      val str = json.as[String]
      JsSuccess(MonthOfYear.withName(str))
    }

    override def writes(o: MonthOfYear): JsValue =
      JsString(o.toString)
  }

}