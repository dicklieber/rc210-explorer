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

/**
 *
 * @param id       this is used in the application logic not HTML <selectOptions><option>
 * @param display  what to show users = <option>s
 * @param selected true if this is currently selected.
 */
case class SelectOption (id: String, display: String, selected: Boolean = false) {
  def html: String = {
    val s: String = if (selected) " selected " else " "
    s"""<option value="$id" $s >$display</option>"""
  }
}

object SelectOption {
  def apply(id:Int, display:String):SelectOption = new SelectOption(id.toString, display)
  def apply(both:String):SelectOption = new SelectOption(both, both)
}