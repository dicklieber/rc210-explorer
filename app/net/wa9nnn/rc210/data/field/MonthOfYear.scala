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
import net.wa9nnn.rc210.data.field.MonthOfYear.options
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.util.{SelectField, SelectOption}

/**
 * An enumeration with behaviour.
 *
 * @param value    one of the display values in selectOptions.
 * @param key      id for name in a <select>.
 */
case class MonthOfYear(key: Key, value: String = options.head.display) extends SelectField {
  val fieldKey: FieldKey = FieldKey("MonthOfYear", key)

  override def update(newValue: String): MonthOfYear = {
    copy(value = newValue)
  }

  def update(newId: Int): MonthOfYear = {
    copy(value = options(newId).display)
  }

  override val selectOptions: Seq[SelectOption] = options
}

object MonthOfYear {
  def apply(key: Key, id: Int): MonthOfYear = {
    new MonthOfYear(key, options(id).display)
  }

  val options: Seq[SelectOption] =
    Seq(
      "Every Month" -> 0,
      "January" -> 1,
      "February" -> 2,
      "March" -> 3,
      "April" -> 4,
      "May" -> 5,
      "June" -> 6,
      "July" -> 7,
      "August" -> 8,
      "September" -> 9,
      "October" -> 10,
      "November" -> 11,
      "December" -> 12,
    ).map { t => SelectOption(t._2, t._1) }
}
