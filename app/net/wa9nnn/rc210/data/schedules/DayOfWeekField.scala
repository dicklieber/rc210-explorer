/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */

package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import play.api.data.FormError
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter
import play.api.libs.json.{Format, Json}

/**
 * Helper to parse DOW, 1 digit and WIM/DOW 2 digits from memory int.
 * @param rc210Value day of week or week-in-month and day of week
 */
case class DayOfWeekField(val rc210Value: Int = 0) extends LazyLogging:
  private val string: String = rc210Value.toString
  val t: (DayOfWeek, WeekInMonth) = if string.length == 1 then
    DayOfWeek.find(string.toInt) -> WeekInMonth.Every
  else
    val wim = string.charAt(0).toInt
    val dow = string.charAt(1).toInt
    DayOfWeek.find(dow) -> WeekInMonth.find(wim)
  val dayOfWeek: DayOfWeek = t._1
  val weekInMonth: WeekInMonth = t._2
  

//object DayOfWeekField:
//  implicit val fmt: Format[DayOfWeekField] = Json.format[DayOfWeekField]
//  implicit val keyFormatter: Formatter[DayOfWeekField] = new Formatter[DayOfWeekField]:
//    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], DayOfWeekField] =
//      parsing(s => DayOfWeekField(s.toInt), s"BadKey $key", Nil)(key, data)
//
//    override def unbind(key: String, value: DayOfWeekField): Map[String, String] = Map(key -> value.toString)
