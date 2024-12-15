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
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}

/**
 *
 * @param fieldDefinition specific to this entry. e.g. template, name etc.
 * @param fieldData       the value. mutable
 */
class FieldEntry(val fieldDefinition: FieldDefinition, initialValue: FieldData)
  extends FieldEntryBase :
  val fieldKey: FieldKey = initialValue.fieldKey
  val template: String = fieldDefinition.template
  private var _fieldData: FieldData = initialValue
  override val toString: String = _fieldData.display

  def set(candidate: FieldValue): Unit =
    _fieldData = _fieldData.setCandidate(candidate)

  def set(fieldData: FieldData): Unit =
    _fieldData = fieldData

  def acceptCandidate(): Unit =
    _fieldData = _fieldData.acceptCandidate()

  def rollBack: Unit =
    _fieldData = _fieldData.rollBack

  def fieldData: FieldData = _fieldData

  def value[F <: FieldValue]: F =
    _fieldData.candidate.getOrElse(_fieldData.fieldValue).asInstanceOf[F]

  def tableSection: TableSection =
    _fieldData.value.tableSection(fieldKey)

  def table: Table =
    //    val value1: Node = value
    //    value1.table(fieldKey)
    throw new NotImplementedError() //todo

  /**
   *
   * @param newFieldValue already parsed to a [[FieldValue]]
   * @return updated [[FieldEntry]].
   */

  def setCandidate(formFieldValue: String): Unit = 
    val newCandidate: SimpleFieldValue = _fieldData.fieldValue.update(formFieldValue)
    _fieldData = _fieldData.setCandidate(newCandidate)

  override def toRow: Row =
    throw new NotImplementedError() //todo

object FieldEntry:
  implicit val ageOrdering: Ordering[FieldEntry] = Ordering.by[FieldEntry, Key](_.fieldKey.key)

  def apply(fieldDefinition: FieldDefinition, fieldKey: FieldKey, fieldValue: FieldValue): FieldEntry =
    new FieldEntry(fieldDefinition, FieldData(fieldKey, fieldValue))

  def header(keyKind: KeyKind): Header = Header(s"$keyKind", "Number", "Field",
    Cell("Value")
      .withToolTip("Either the candidate or current value."),
    Cell("Change")
      .withToolTip("Shows how the current value will becomes the candidate.")
  )

  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Value")

