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

package net.wa9nnn.rc210.util

import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.key.KeyFactory.MacroKey

object MacroSelect extends Selectable {
  val choices: Seq[SelectItem] = Seq.empty // only options need  be used.


  override def options: Seq[(String, String)] = {
    KeyFactory[MacroKey](KeyKind.macroKey).map { macroKey => macroKey.toString -> macroKey.keyWithName
    }
  }
}

case class MacroItem(macroKey: MacroKey) extends SelectItem {

  override def item: (String, String) = {
    macroKey.toString -> display
  }

  override val display: String = macroKey.keyWithName

  /**
   *
   * @param formValue as seledcted by user in form.
   * @return
   */
  override def isSelected(formValue: String): Boolean = formValue == macroKey.toString

  /**
   *
   * @param number from RC-210 data
   * @return
   */
  override def isSelected(number: Int): Boolean = throw new IllegalStateException("")
}
