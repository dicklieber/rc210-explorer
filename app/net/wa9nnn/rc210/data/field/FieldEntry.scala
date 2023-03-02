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

case class FieldEntry(fieldValue: FieldValue, fieldMetadata: FieldMetadata) extends RowSource{
  def fieldKey: FieldKey = fieldValue.fieldKey

  override def toRow: Row = Row(
    fieldKey.toCell,
    fieldMetadata.offset,
    fieldMetadata.extractor.name,
    fieldMetadata.uiRender,
    fieldMetadata.selectOptions.map(_.toString()),
    fieldMetadata.template,
    Cell(fieldValue.current)
      .withCssClass(fieldValue.cssClass),
    "//todo"
  )
}

object FieldEntry {
  def header(count:Int)= Header(s"Fields ($count)", "FieldKey", "Offset", "Extractor", "UI", "Select Options", "Template", "Value", "Command")
}