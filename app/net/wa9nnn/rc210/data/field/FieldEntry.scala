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
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import os.copy
import play.api.libs.json.{Format, Json, OFormat}

/**
 *
 * @param fieldDefinition  specific to this entry. e.g. template, name etc.
 * @param initialFieldData what we start with.
 */
class FieldEntry(fieldDefinition: FieldDefinition, initialFieldData: FieldData)
  extends Ordered[FieldEntry] with FieldEntryBase:
  private val fieldKey = initialFieldData.fieldKey
  private var fieldData: FieldData = initialFieldData

  override def toString: String = fieldKey.display
  def hasCandidate: Boolean = fieldData.candidate.isDefined
  def setCandidate(fieldValue: FieldValue): Unit =
    fieldData = fieldData.setCandidate(fieldValue)
  def setCandidate(str:String): Unit =
    val fv:FieldValue = try
      FieldInt(str.toInt)
    catch
      case e:Throwable =>
        FieldString(str)
        
    fieldData = fieldData.setCandidate(fv)

  def acceptCandidate(): Unit =
    fieldData = fieldData.acceptCandidate()

  def clearCandidate(): Unit =
    fieldData = fieldData.clearCandidate()

  def setFieldData(fieldData: FieldData): Unit =
    this.fieldData = fieldData

  def tableSection: TableSection =
    fieldData.value.tableSection(fieldKey)

  def valueDisplayCell: Cell =
    fieldData.value.displayCell

  def candidateDisplayCell: Cell =
    fieldData.candidate.map(_.displayCell).getOrElse(Cell(""))

  def commands: Seq[String] =
    fieldData.candidate
      .getOrElse(throw new IllegalStateException(s"No candidate for: $fieldKey!"))
      .toCommands(this)

  def toEditCell: Cell =
    fieldData.value.toEditCell(fieldKey)

  def value[T <: FieldValue]: T =
    fieldData.value.asInstanceOf

  def toJson: FieldEntryJson =
    fieldData

  override def compare(that: FieldEntry): Int = fieldKey compare that.fieldKey

  override val template: String = fieldDefinition.template

  override def toRow: Row =
    fieldData.value.toRow

object FieldEntry:

  def apply(complexExtractor: ComplexExtractor[?], complexFieldValue: ComplexFieldValue): FieldEntry =
    FieldEntry(complexExtractor, complexFieldValue.fieldKey)

  def header(keyKind: KeyKind): Header = Header(s"$keyKind", "Number", "Field",
    Cell("Value")
      .withToolTip("Either the candidate or current value."),
    Cell("Change")
      .withToolTip("Shows how the current value will becomes the candidate.")
  )

  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Value")


