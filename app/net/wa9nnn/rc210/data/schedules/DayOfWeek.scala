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

object DayOfWeek extends Enumeration {
  type DayOfWeek = Value
  val EveryDay,
  Monday,
  Tuesday,
  Wednesday,
  Thursday,
  Friday,
  Saturday,
  Sunday,
  Weekdays,
  Weekends = Value

  implicit val fmtDayOfWeek: Format[DayOfWeek] = new Format[DayOfWeek] {
    override def reads(json: JsValue): JsResult[DayOfWeek] = {
      val str = json.as[String]
      JsSuccess(DayOfWeek.withName(str))
    }

    override def writes(o: DayOfWeek): JsValue =
      JsString(o.toString)
  }
}
