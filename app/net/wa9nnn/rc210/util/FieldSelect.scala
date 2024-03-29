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

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.field.{FieldEntryBase, SimpleFieldValue}
import play.api.libs.json.{JsString, JsValue}

/**
 * An enumeration with behaviour.
 */
trait FieldSelect[T] extends SimpleFieldValue {
  def selectOptions: Seq[SelectOption]
  val name: String
  val value: T

  override def displayCell: Cell = Cell(value)

  /**
   * Render this value as an RC-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey
    val key: Key = fieldKey.key

    val number = try {
      value match {
        case s: String =>
          selectOptions.find(_.display == s).get.id
        case m: Key =>
          m.rc210Value
      }
    } catch {
      case e:Exception =>
        logger.error(s"Getting number for: $value", e)
    }
    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", number.toString))

  }


  override def toJsValue: JsValue = JsString(value.toString)

//  /**
//   *
//   * Render as HTML. Either a single rc2input of an entire HTML Form.
//   *
//   */
//  override def toHtmlField(renderMetadata: RenderMetadata): String = {
//
//    val optionsHtml: String = selectOptions.map { selectOption =>
//      val bool = selectOption.display == value.toString
//      selectOption.copy(selected = bool).html
//    }.mkString("\n")
//    val param: String = renderMetadata.param
//
//    s"""
//    <selectOptions name="$param" class="form-selectOptions" aria-label="Default selectOptions example">
//    $optionsHtml
//    </selectOptions>
//    """.stripMargin
//  }

}




trait FieldSelectComp {
  val name: String
  val options: Seq[SelectOption]
}


