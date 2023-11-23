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

package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.util.{FieldSelect, SelectOption}
import play.api.libs.json.JsValue

/**
 * An enumeration with behaviour.
 *
 * @param value    one of the display values in DayOfWeek.options.
 */
case class TimeoutTimerResetPoint(value: String = selectOptions.head.display) extends FieldSelect[String] {
  override val selectOptions: Seq[SelectOption] = TimeoutTimerResetPoint.selectOptions
  override val name: String = TimeoutTimerResetPoint.name

  override def update(paramValue: String): TimeoutTimerResetPoint = {
    TimeoutTimerResetPoint(paramValue)
  }
}

object TimeoutTimerResetPoint extends SimpleExtractor {

  def apply(id: Int): TimeoutTimerResetPoint = {
    val maybeOption = selectOptions.find(_.id == id.toString)
    new TimeoutTimerResetPoint(maybeOption.get.display)
  }

  val selectOptions: Seq[SelectOption] =
    Seq(
      "After COS" -> 0,
      "After CT Segment 1" -> 1,
      "After CT Segment 2" -> 2,
      "After CT Segment 3" -> 3,
      "After CT Segment 4" -> 4
    ).map { t => SelectOption(t._2, t._1) }

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): TimeoutTimerResetPoint = {
    val id = itr.next()
    apply(id)
  }

  override def parse(jsValue: JsValue): FieldValue = new TimeoutTimerResetPoint(jsValue.as[String])

  override val name: String = "TOT Reset Point"

}
