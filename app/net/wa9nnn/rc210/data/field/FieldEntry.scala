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

import com.wa9nnn.util.tableui._
import net.wa9nnn.rc210.data.FieldKey
import play.api.libs.json.{JsValue, Json}


/**
 *
 * @param fieldDefinition specific to this entry. e.g. template, name etc.
 * @param fieldValue      the value.
 * @param candidate       the,potential, next value.
 */
case class FieldEntry(fieldDefinition: FieldDefinition, fieldKey: FieldKey, fieldValue: FieldValue, candidate: Option[FieldValue] = None)
  extends RowSource with Ordered[FieldEntry] with CellProvider with RenderMetadata {

  def value[F<: FieldValue]:F = {
    candidate.getOrElse(fieldValue).asInstanceOf[F]
  }


  override val unit: String = fieldDefinition.uiInfo.unit

  def setCandidate(newValue: String): FieldEntry = {
     copy(candidate = Option(fieldValue.update(newValue)))
  }

  def acceptCandidate(): FieldEntry = copy(
    candidate = None,
    fieldValue = candidate.getOrElse(throw new IllegalStateException(s"No candidate to accept!")))

  val param: String = fieldKey.param
  val prompt: String = fieldDefinition.prompt

  def toCommand: String = fieldValue.toCommand(this)

  def toHtml: String = {
    fieldValue.toHtmlField(this)
  }

  def toCell: Cell = {
    Cell.rawHtml(s"$toHtml")
  }

  override def toString: String = fieldValue.toString

  override def toRow: Row = Row(
    fieldKey.toCell,
    fieldValue.display,
    candidate.map(_.toString).getOrElse("-")
  )
  //    Cell("")
  //      .withImage(routes.Assets.versioned("images/pencil-square.png").url)
  //      //      .withUrl(routes.FieldEditorController.editOne(fieldKey.param).url)
  //      .withToolTip("Edit this field")
  //  )

  override def compare(that: FieldEntry): Int = fieldKey compare that.fieldKey

  def toJson: JsValue = {
    Json.obj(
      "value" -> fieldValue.toJsValue,
      //      "candidate" -> candidate.map(_.toJsValue).getOrElse(JsNull)
    )
  }
}


object FieldEntry {
  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Value")
}