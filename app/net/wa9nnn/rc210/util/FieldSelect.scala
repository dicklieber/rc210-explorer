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

import net.wa9nnn.rc210.data.field.{FieldEntry, FieldEntryBase, RenderMetadata, SimpleFieldValue}
import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.key.KeyFactory.MacroKey
import play.api.libs.json.{JsString, JsValue}

/**
 * An enumeration with behaviour.
 */
trait FieldSelect[T] extends SimpleFieldValue {
  val selectOptions: Seq[SelectOption]
  val name: String
  val value: T

  /**
   * This only works with String typw [[FieldSelect]]s.
   * The only non-String is the [[MacroSelect]], but there the value:T is [[MacroKey]].
   *
   * @return
   */
  def number: Int = selectOptions.find(selectOption =>
    if (value.isInstanceOf[String])
      selectOption.display == value.asInstanceOf[String]
    else
      throw new IllegalStateException("THis onlly works for String selects.")
  )
    .map(_.id)
    .getOrElse(1)

  override def display: String = value.toString

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey
    val key: KeyFactory.Key = fieldKey.key

    val number = try {
      value match {
        case s: String =>
          selectOptions.find(_.display == s).get.id
        case m: MacroKey =>
          m.number
      }
    } catch {
      case e:Exception =>
        logger.error(s"Getting number for: $value", e)
    }
    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", number.toString))

  }


  override def toJsonValue: JsValue = JsString(display)

  /**
   *
   * Render as HTML. Either a single rc2input of an entire HTML Form.
   *
   */
  override def toHtmlField(renderMetadata: RenderMetadata): String = {

    val optionsHtml: String = selectOptions.map { selectOption: SelectOption =>
      val bool = selectOption.display == value.toString
      selectOption.copy(selected = bool).html
    }.mkString("\n")
    val param: String = renderMetadata.param

    s"""
    <select name="$param" class="form-select" aria-label="Default select example">
    $optionsHtml
    </select>
    """.stripMargin
  }

  override def toString: String = display
}


/**
 *
 * @param id       this is used in the application logic not HTML <select><option>
 * @param display  what to show  users and use as value attribute in <option>s
 * @param selected true if this is currently selected.
 */
case class SelectOption protected(id: Int, display: String, selected: Boolean = false) {
  def html: String = {
    val s: String = if (selected) " selected " else " "
    s"""<option value="$display" $s >$display</option>"""
  }
}

trait FieldSelectComp {
  val name: String
  val options: Seq[SelectOption]
}


