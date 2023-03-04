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

import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.data.FieldKey

import scala.jdk.CollectionConverters._

/**
 * Whats needed to work with a [[FieldValue]]
 *  - render as an RC-210 command string.
 *  - render as an  HTML input. Using a Twirl template.
 *  - Validate a field from HTML form value.
 *
 * @param fieldKey      id for this field.
 * @param template      for an RC-210 Command string
 * @param uiType        render hint.
 * @param selectOptions required if [[uiType]] is select.
 */
case class FieldMetadataXYZZY(fieldKey: FieldKey, template: String, selectOptions: Option[UiSelect]) {



  /**
   * template e.g. "$n*2121$b" becomes "3*21210" or  "*2091$b" becomes "*2091$1"
   *
   * @param fieldValue to render.
   * @return
   */
  def command(fieldValue: FieldValue): String = {
    assert(fieldValue.dirty, "No candidate for command!*")
    val sValue = fieldValue.candidate.getOrElse(throw new IllegalStateException("No candidate to send!"))
    val bool: String = if (sValue == "true") "1"
    else
      "0"
    import org.apache.commons.text.StringSubstitutor
    lazy val map: Map[String, String] = Seq(
      "v" -> sValue,
      "b" -> bool,
      "n" -> fieldValue.fieldKey.key.number.toString
    ).toMap
    val substitutor = new StringSubstitutor(map.asJava, "$", "")

    //todo color token and replacement parts <span> s
    substitutor.replace(template)
  }

  def toRow(fieldValue:Option[ FieldValue]):Row = {
    Row(fieldKey.toCell,fieldKey.key.toCell, fieldValue.map(_.value).getOrElse("Missing") )
  }

}


