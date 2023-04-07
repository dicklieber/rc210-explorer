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

import net.wa9nnn.rc210.data.field
import net.wa9nnn.rc210.data.field.MuteDigit.selectOptions
import net.wa9nnn.rc210.util.{FieldSelect, SelectOption}
import play.api.libs.json.JsValue

/**
 * An enumeration with behaviour.
 *
 * @param value    one of the display values in DayOfWeek.options.
 */
case class MuteDigit(value: String = selectOptions.head.display) extends FieldSelect[String] {
  override val selectOptions: Seq[SelectOption] = MuteDigit.selectOptions
  override val name: String = DayOfWeek.name

  override def update(paramValue: String): FieldValue = {
    MuteDigit(paramValue)
  }

}

object MuteDigit extends SimpleExtractor {

  def apply(id: Int): MuteDigit = {
    val maybeOption = selectOptions.find(_.id == id)
    new MuteDigit(maybeOption.get.display)
  }

  val selectOptions: Seq[SelectOption] =
    Seq(
      "1st digit" -> 1,
      "2nd digit" -> 2
    ).map { t => SelectOption(t._2, t._1) }

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): MuteDigit = {
    val id = itr.next()
    apply(id)
  }

  override def jsonToField(jsValue: JsValue): FieldValue = new  MuteDigit(jsValue.as[String])

  override val name: String = "MuteDigit"
}
