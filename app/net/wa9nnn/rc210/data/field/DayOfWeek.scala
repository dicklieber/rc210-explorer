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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.util.{FieldSelect, SelectOption}
import DayOfWeek._
/**
 * An enumeration with behaviour.
 *
 * @param value    one of the display values in selectOptions.
 * @param key      id for name in a <select>.
 */
case class DayOfWeek(key: Key, value: String = options.head.display) extends FieldSelect[String] {

  override val fieldKey: FieldKey = FieldKey("DayOfWeek", key)

  override def update(newValue: String): DayOfWeek = {
    copy(value = newValue)
  }

  def update(newId: Int): DayOfWeek = {
    options.find(_.id == newId).map{option =>
      copy(value = option.display)
    }.getOrElse(throw new IllegalArgumentException(s"No DayOfWeek id of: $newId"))
  }

  override val selectOptions: Seq[SelectOption] = options
}

object DayOfWeek {
  def apply(key: Key, id: Int): DayOfWeek = {
    new DayOfWeek(key, options(id).display)
  }

  val options: Seq[SelectOption] =
    Seq(
      "EveryDay" -> 0,
      "Monday" -> 1,
      "Tuesday" -> 2,
      "Wednesday" -> 3,
      "Thursday" -> 4,
      "Friday" -> 5,
      "Saturday" -> 6,
      "Sunday" -> 7,
      "Weekdays" -> 8,
      "Weekends" -> 9
    ).map { t => SelectOption(t._2, t._1) }
}
