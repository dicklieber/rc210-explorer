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

import com.wa9nnn.util.tableui.{Cell, Header, Row, RowSource}
import net.wa9nnn.rc210.data.FieldKey


case class FieldEntry(fieldValue: FieldValue, fieldMetadata: FieldMetadata) extends RowSource {
  def fieldKey: FieldKey = fieldValue.fieldKey

  override def toRow: Row = Row(
    fieldKey.prettyName,
    fieldKey.key.toCell,
    fieldMetadata.offset,
    fieldMetadata.uiInfo.fieldExtractor.name,
    fieldMetadata.uiInfo.uiRender,
    fieldMetadata.uiInfo.toString,
    fieldMetadata.selectOptions.map(_.toString()),
    fieldMetadata.template,
    Cell(fieldValue.current)
      .withCssClass(fieldValue.cssClass),
    command
  )

  def command: String = {
    val bool: String = if (fieldValue.current == "true") "1"
    else
      "0"
    val map = Seq(
      "V" -> fieldValue.current,
      "B" -> bool,
      "N" -> fieldValue.fieldKey.key.number.toString
    ).toMap

    map.foldLeft(fieldMetadata.template) { (command: String, tr: (String, String)) =>
      val str = command.replaceAll(tr._1, tr._2)
      str
    }
    //todo color token and replacement parts <span> s
  }
}


object FieldEntry {
  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Offset", "Extractor", "render", "UI", "Select Options", "Template", "Value", "Command")
}