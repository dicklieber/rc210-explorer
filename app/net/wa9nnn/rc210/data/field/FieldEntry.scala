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
import net.wa9nnn.rc210.data.Node
import net.wa9nnn.rc210
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import os.copy
import play.api.libs.json.{Format, Json, OFormat}

/**
 *
 * @param fieldDefinition  specific to this entry. e.g. template, name etc.
 * @param initialFieldData what we start with.
 */
class FieldEntry(fieldDefinition: FieldDefinition, initialFieldData: FieldData)
  extends FieldEntryBase:
  val fieldKey = initialFieldData.fieldKey
  private var _fieldData: FieldData = initialFieldData
  def fieldData: FieldData = _fieldData

  override def toString: String = fieldKey.display
  def hasCandidate: Boolean = _fieldData.candidate.isDefined
  def setCandidate(fieldValue: FieldValue): Unit =
    _fieldData = _fieldData.setCandidate(fieldValue)
  def setCandidate(str:String): Unit =
    val fv:FieldValue = try
      FieldInt(str.toInt)
    catch
      case e:Throwable =>
        FieldString(str)
        
    _fieldData = _fieldData.setCandidate(fv)

  def acceptCandidate(): Unit =
    _fieldData = _fieldData.acceptCandidate()

  def clearCandidate(): Unit =
    _fieldData = _fieldData.clearCandidate

  def setFieldData(fieldData: FieldData): Unit =
    this._fieldData = fieldData

  def tableSection: TableSection =
    _fieldData.value.tableSection(fieldKey)

  def valueDisplayCell: Cell =
    _fieldData.value.displayCell

  def candidateDisplayCell: Cell =
    _fieldData.candidate.map(_.displayCell).getOrElse(Cell(""))

  def commands: Seq[String] =
    _fieldData.candidate
      .getOrElse(throw new IllegalStateException(s"No candidate for: $fieldKey!"))
      .toCommands(this)

  def toEditCell: Cell =
    _fieldData.value.toEditCell(fieldKey)

  def value[T <: FieldValue]: T =
    _fieldData.value.asInstanceOf

  override val template: String = fieldDefinition.template

  override def toRow: Row =
    _fieldData.value.toRow

object FieldEntry:

  def apply(fieldDefinition: FieldDefinition, fieldValue: FieldValue): FieldEntry =
    FieldEntry(fieldDefinition, fieldValue)

  def header(keyKind: KeyKind): Header = Header(s"$keyKind", "Number", "Field",
    Cell("Value")
      .withToolTip("Either the candidate or current value."),
    Cell("Change")
      .withToolTip("Shows how the current value will becomes the candidate.")
  )

  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Value")


