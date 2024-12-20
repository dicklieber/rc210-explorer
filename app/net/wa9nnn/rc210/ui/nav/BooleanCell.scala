package net.wa9nnn.rc210.ui.nav

import com.wa9nnn.wa9nnnutil.tableui.Cell

/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */


object BooleanCell:
    def apply(boolean: Boolean): Cell =
      val s: String = if (boolean) then
        """<i class="bi bi-check-square-fill"></i>"""
      else
      
        """<i class="bi bi-square"></i>"""
      
      Cell.rawHtml(s)
        .withCssClass("checkbox")


