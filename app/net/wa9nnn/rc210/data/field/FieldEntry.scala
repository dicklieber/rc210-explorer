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

import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.data.datastore.FieldEntryJson
import net.wa9nnn.rc210.data.Node
import net.wa9nnn.rc210
import net.wa9nnn.rc210.KeyKind{FieldKey, Key, KeyKind}
import play.api.libs.json.{Format, Json, OFormat}

/**
 *
 * @param fieldDefinition specific to this entry. e.g. template, name etc.
 * @param fieldValue      the value.
 * @param candidate       the,potential, next value.
 */
case class FieldEntry(fieldDefinition: FieldDefinition, fieldKey: FieldKey, fieldValue: FieldValue, candidate: Option[FieldValue] = None)
  extends Ordered[FieldEntry] with FieldEntryBase {
  override def toString: String = fieldKey.display

  def value[F <: FieldValue]: F = {
    candidate.getOrElse(fieldValue).asInstanceOf[F]
  }

  def tableSection: TableSection =
    value[FieldValue].tableSection(fieldKey)

  def table: Table =
    val value1: Node = value
    value1.table(fieldKey)

  /**
   *
   * @param newFieldValue already parsed to a [[FieldValue]]
   * @return updated [[FieldEntry]].
   */
  def setCandidate(newFieldValue: ComplexFieldValue): FieldEntry = {
    if (fieldValue == newFieldValue)
      copy(candidate = None)
    else
      copy(candidate = Option(newFieldValue))
  }

  def setCandidate(formValue: String): FieldEntry = {
    val simpleFieldValue = fieldValue.asInstanceOf[SimpleFieldValue]
    val updatedFieldValue: SimpleFieldValue = simpleFieldValue.update(formValue)

    if (updatedFieldValue == fieldValue)
    {
      copy(candidate = None)
    }
    else
    {
      copy(candidate = Option(updatedFieldValue))
    }
  }

  def acceptCandidate(): FieldEntry = copy(
    candidate = None,
    fieldValue = candidate.getOrElse(fieldValue))

  def commands: Seq[String] = {
    candidate
      .getOrElse(throw new IllegalStateException(s"No candidate for: $fieldKey!"))
      .toCommands(this)
  }

  def toJson: FieldEntryJson = {
    FieldEntryJson(this)
  }

  override def compare(that: FieldEntry): Int = fieldKey compare that.fieldKey

  override val template: String = fieldDefinition.template

  override def toRow: Row = {
    val value1: FieldValue = value
    value1.toRow
  }
}

case class FieldData(fieldKey: FieldKey, fieldValue: FieldValue, candidate: Option[FieldValue] = None)
object FieldEntry {
  implicit val fmt: OFormat[FieldEntry] = Json.format[FieldEntry]

  def apply(complexExtractor: ComplexExtractor[?], complexFieldValue: ComplexFieldValue): FieldEntry = {

    new FieldEntry(complexExtractor, complexFieldValue.fieldKey, complexFieldValue)
  }

  def header(keyKind: KeyKind): Header = Header(s"$keyKind", "Number", "Field",
    Cell("Value")
      .withToolTip("Either the candidate or current value."),
    Cell("Change")
      .withToolTip("Shows how the current value will becomes the candidate.")
  )

  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Value")

}

