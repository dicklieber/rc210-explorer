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

import net.wa9nnn.rc210.util.{FieldSelect, FieldSelectComp, SelectOption}
import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue}

/**
 * An enumeration with behaviour.
 *
 * @param value    one of the display values in selectOptions.
 */
case class WeekInMonth(value: String = WeekInMonth.options.head.display) extends FieldSelect[String] {


  override val selectOptions: Seq[SelectOption] = WeekInMonth.options
  override val name: String = DayOfWeek.name

  override def update(paramValue: String): FieldValue = {
    WeekInMonth(paramValue)
  }
}

object WeekInMonth extends FieldSelectComp {
  override val name: String = "WeekInMonth"

  def apply(id: Int): WeekInMonth = {
    val maybeOption = options.find(_.id == id)
    new WeekInMonth(maybeOption.get.display)
  }

  /**
   *
   * @param valueMap from form data for this key
   * @return
   */
  def apply()(implicit valueMap: Map[String, String]): WeekInMonth = {
    val str = valueMap(name)
    new WeekInMonth(str)
  }

  val options: Seq[SelectOption] =
    Seq(
      "Every Week" -> 0,
      "1st Week" -> 1,
      "2nd Week" -> 2,
      "3rd Week" -> 3,
      "4th Week" -> 4,
      "5th Week" -> 5,
    ).map { t => SelectOption(t._2, t._1) }
  implicit val fmtDayOfWeek: Format[WeekInMonth] = new Format[WeekInMonth] {

    override def writes(o: WeekInMonth) = o.toJsonValue

    override def reads(json: JsValue): JsResult[WeekInMonth] = JsSuccess(WeekInMonth(json.as[String]))
  }

}
