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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{FieldContents, FieldEntry}
import play.api.libs.json.JsValue


case class SelectField(metadata: SelectFieldMetadata, currentValue: String) extends FieldContents {
  override def toJsValue: JsValue = ???

  override def display: String = currentValue

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  /**
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   * @param fieldEntry all the metadata.
   * @return html
   */
  override def toHtmlField(fieldEntry: FieldEntry): String = ???
}

object SelectField {
  def apply(options: Seq[(String, Int)], fieldKey: FieldKey, current: Int): SelectField = {

    val opts = options.map { case (display, id) =>
      SelectOption(id, display)
    }


    val metadata = new SelectFieldMetadata(fieldKey, opts)
    new SelectField(metadata, metadata(current))
  }
}


/**
 * Holds the current enumerated value.
 * This is yet another enumeration value; hava enum don't allow adding scala behaviour and passing geneaic enums damn near impossible.
 * Scala Enumeration in scala 2 is bizarre and is going away in scala 3.
 * So we invent yet another enumeration but it does all the things we need for this application.
 *
 * @param fieldKey for this field.
 * @param strings  the strings leading empty string are use dto 'take up' zero.
 */
class SelectFieldMetadata(fieldKey: FieldKey, selectOptions: Seq[SelectOption]) {
  def toHtml(fieldEntry: FieldEntry): String = {
    val currentValue = fieldEntry.fieldValue.asInstanceOf[SelectField].currentValue

    val optionsHtml: String = selectOptions.map { selectOption: SelectOption =>
      selectOption.copy(selected = selectOption.display == currentValue).html
    }.mkString("\n")
    val param = fieldEntry.param

    s"""
    <select name="$param" class="form-select" aria-label="Default select example" title="${fieldEntry.prompt}">
    $optionsHtml
    </select>
    """.stripMargin
  }

  /**
   * find display string for an id.
   *
   */
  def apply(id: Int): String = {
    selectOptions.find(_.id == id)
      .map(_.display)
      .getOrElse(throw new IllegalStateException(s"No id $id for $fieldKey"))
  }
}

/**
 *
 * @param id       this is used in the application logic not HTML <select><ption>
 * @param display  what to show  users and use as value attribute in <option>s
 * @param selected true if this is currently selected.
 */
case class SelectOption protected(id: Int, display: String, selected: Boolean = false) {
  def html: String = {
    val s: String = if (selected) " selected " else " "
    s"""<option value="$display" >$display</option>"""
  }
}

