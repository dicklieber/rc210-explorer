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

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.{Key, KeyMetadata}

/**
 * There is one of these for each element in the [[net.wa9nnn.rc210.data.datastore.DataStore]]
 *
 * @param key             primary key for this 
 * @param fieldDefinition specific to this entry. e.g. template, name etc.
 * @param fieldData       the value. mutable
 */
class FieldEntry( val fieldDefinition: FieldDef[?], initialValue: FieldData) extends LazyLogging:
  val key: Key = initialValue.key
//  val template: String = fieldDefinition.template
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
    _fieldData.value.tableSection(key)

  def table: Table =
    //    val value1: Node = value
    //    value1.table(fieldKey)
    throw new NotImplementedError() //todo

  /**
   *
   * @param newFieldValue already parsed to a [[FieldValue]]
   * @return updated [[FieldEntry]].
   */


  def toCommands: Seq[String] =
    value[FieldValue] match
      case complex: FieldValueComplex[?] =>
        complex.toCommands(key)
      case simple: FieldValueSimple =>
        Seq(simple.toCommand(key, fieldDefinition.asInstanceOf[FieldDefSimple[?]].template))
 

object FieldEntry:
  implicit val ordering: Ordering[FieldEntry] = Ordering.by[FieldEntry, Key](_.key)

  def apply(fieldDefinition: FieldDef[?], key: Key, fieldValue: FieldValue): FieldEntry =
    new FieldEntry(
      fieldDefinition = fieldDefinition,
      initialValue = FieldData(key, fieldValue))

  def header(keyKind: KeyMetadata): Header = Header(s"$keyKind", "Number", "Field",
    Cell("Value")
      .withToolTip("Either the candidate or current value."),
    Cell("Change")
      .withToolTip("Shows how the current value will becomes the candidate.")
  )

  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Value")

