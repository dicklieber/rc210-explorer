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

package net.wa9nnn.rc210.ui

import com.wa9nnn.util.tableui.Cell
import play.api.mvc.Call

object EditButton {
  /**
   * Cwell with abuttoin to edit the item.
    * @param call to nmake when button is clicked.
   * @return
   */
  def apply(call: Call): Cell = {
    val url = call.url

    Cell.rawHtml(
      s"""<button type="button" class="bi bi-pencil-square btn p-0"
         |                    onclick="window.location.href='$url'"></button>""".stripMargin)
  }

}
