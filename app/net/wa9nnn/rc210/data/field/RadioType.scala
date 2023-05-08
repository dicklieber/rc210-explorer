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

import net.wa9nnn.rc210.data.field.RadioType.selectOptions
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.util.{FieldSelect, SelectOption}
import play.api.libs.json.JsValue

/**
 * An enumeration with behaviour.
 *
 * @param value    one of the display values in DayOfWeek.options.
 */
case class RadioType(value: String = selectOptions.head.display) extends FieldSelect[String] {
  override val selectOptions: Seq[SelectOption] = RadioType.selectOptions
  override val name: String = RadioType.name

  override def update(paramValue: String): RadioType = {
    RadioType(paramValue)
  }
}

object RadioType extends SimpleExtractor[String] {

  def apply(id: Int): RadioType = {
    val maybeOption = selectOptions.find(_.id == id)
    new RadioType(maybeOption.get.display)
  }

  val selectOptions: Seq[SelectOption] =
    Seq(
      "Kenwood" -> 1,
      "Icom" -> 2,
      "Yaesu" -> 3,
      "Kenwood V7a" -> 4,
      "Doug Hall RBI - 1" -> 5,
      // there is no 6
      "Kenwood g707" -> 7,
      "Kenwood 271A" -> 8,
      "Kenwood V71a" -> 9).map { t => SelectOption(t._2, t._1) }

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): RadioType = {
    val id = itr.next()
    apply(id)
  }

  override def parse(jsValue: JsValue): FieldValue = new RadioType(jsValue.as[String])

  override val name: String = "RadioType"

  override def fromForm(name: String)(implicit kv: Map[String, String],key: Key): String = {
    formValue(name)
  }
}
