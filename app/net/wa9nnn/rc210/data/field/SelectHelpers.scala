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

import net.wa9nnn.rc210.key.Key

import java.util

object SelectEnumerationHelper {
  def apply(idValue: String, enumeration: Enumeration): enumeration.Value = {
    enumeration.withName(idValue)
  }

  /**
   * Generate HTML for an [[Enumeration]]
   *
   * @param enumeration for field.
   * @param current     it's current value.
   * @param param       will be key for POSTed data.
   * @return
   */
  def apply[T](enumeration: Enumeration, current: String, param: String): String = {
    val options: Seq[SelectOption2] = enumeration.values.toSeq.map { e: enumeration.Value =>
      val opt = SelectOption2(e.toString, e.toString)
      if (e.toString == current)
        opt.select
      else
        opt
    }
    views.html.fieldSelect(param, options).toString()
  }
}


object SelectKeyHelper {
  /**
   * Generate an Html string.
   *
   * @param current key, will be selected in <select>
   * @param param   will be key for POSTed data.
   * @return the HtmL fragment for this field.
   */
  def apply(current: Key, param: String): String = {

    val keys: Seq[Key] = current.kind.allKeys
    val options = keys.map { k: Key =>
      val opt = SelectOption2(k.toString, k.toString)
      if (k == current)
        opt.select
      else
        opt
    }
    views.html.fieldSelect(param, options).toString()
  }
}