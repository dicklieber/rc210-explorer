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

import scala.reflect.ClassTag

/**
 * Creates a [[Cell]] with <select>
 * and parses the value from the form back into an Enum.
 *
 * @param param  to be used as the name of the <select> element.
 * @param values from the enum
 * @param classTag$E$0
 * @tparam E the Enum type.
 */
class EnumSelect[E <: Enum[E] : ClassTag](param: String, values: Array[E]) {
  private val options = values.map(_.toString)

  def toCell(current: E): Cell = {
    val html = views.html.fieldSelect(param, current.toString, options).toString()
    Cell.rawHtml(html)
  }

  def toCell: Cell = {
    val html = views.html.fieldSelect(param, "", options).toString()
    Cell.rawHtml(html)
  }

  def fromForm(in: String): E = {
    val clazz = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    Enum.valueOf(clazz, in)
  }


}





