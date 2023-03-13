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
import controllers.routes
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.named.NamedSource
import play.twirl.api.Html


case class FieldEntry(fieldValue: FieldValue, fieldMetadata: FieldMetadata) extends RowSource with Ordered[FieldEntry] {
  val fieldKey: FieldKey = fieldValue.fieldKey
  val param: String = fieldKey.param
  val prompt:String = fieldMetadata.prompt


  def toHtml()(implicit namedSource: NamedSource):String ={
    fieldMetadata.fieldHtml(fieldKey, fieldValue.contents)
  }

  override def toString: String = fieldValue.toString

  override def toRow: Row = Row(
    fieldKey.fieldName,
    fieldKey.key.toCell,
    Cell(fieldValue.current)
      .withCssClass(fieldValue.cssClass),
    Cell("")
      .withImage(routes.Assets.versioned("images/pencil-square.png").url)
      //      .withUrl(routes.FieldEditorController.editOne(fieldKey.param).url)
      .withToolTip("Edit this field")
  )

  override def compare(that: FieldEntry): Int = fieldKey compare that.fieldKey
}


object FieldEntry {
  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Value")
}