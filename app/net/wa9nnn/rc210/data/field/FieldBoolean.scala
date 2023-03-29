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

import com.wa9nnn.util.tableui.Cell
import net.wa9nnn.rc210.data.field
import play.api.libs.json.{JsBoolean, JsValue}
import views.html.fieldCheckbox

case class FieldBoolean(value: Boolean = false) extends FieldValue {
  override def toJsValue: JsValue = JsBoolean(value)

  override def toHtmlField(renderMetadata: RenderMetadata): String = {
    fieldCheckbox(value, renderMetadata).toString()
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def display: String = value.toString

   def toCell(name:String, renderMetadata: RenderMetadata): Cell = {
     super.toCell(renderMetadata)
   }
}

object FieldBoolean {
  def apply(name: String)(implicit nameToValue: Map[String, String]): FieldBoolean = {
    val sBool = nameToValue(name)
    new FieldBoolean(sBool == "true")
  }
}