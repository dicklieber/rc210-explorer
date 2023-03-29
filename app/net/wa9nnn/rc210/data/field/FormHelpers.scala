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

import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.util.SelectOption




object SelectKeyHelper {
  /**
   * Generate an Html string.
   *
   * @param current key, will be selected in <select>
   * @param param   will be key for POSTed data.
   * @return the HtmL fragment for this field.
   */
  def apply(current: Key, param: String): String = {

    val keys: Seq[Key] = KeyFactory(current.kind)

    val options = keys.map { k: Key =>
      val opt = SelectOption(k.number, k.toString)
      if (k == current)
        opt.copy(selected = true)
      else
        opt
    }
    throw new NotImplementedError() //todo
    //    views.html.fieldSelect(param, options).toString()
  }

  def apply[T](name: String)(implicit map: Map[String, Seq[String]]): T = {
    val value: Seq[String] = map(name)
    KeyFactory(value.head)
  }
}
