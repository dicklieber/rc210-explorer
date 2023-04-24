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
import net.wa9nnn.rc210.data.datastore.FieldEntryJson
import net.wa9nnn.rc210.key.KeyFactory.MacroKey
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.model.TriggerNode


/**
 *
 * @param fieldDefinition specific to this entry. e.g. template, name etc.
 * @param fieldValue      the value.
 * @param candidate       the,potential, next value.
 */
case class FieldEntry(fieldDefinition: FieldDefinition, fieldKey: FieldKey, fieldValue: FieldValue, candidate: Option[FieldValue] = None)
  extends Ordered[FieldEntry] with CellProvider with RenderMetadata with FieldEntryBase {

  def value[F <: FieldValue]: F = {
    candidate.getOrElse(fieldValue).asInstanceOf[F]
  }

  /**
   *
   * @param newFieldValue already parsed to a [[FieldValue]]
   * @return updated [[FieldEntry]].
   */
  def setCandidate(newFieldValue: ComplexFieldValue[_]): FieldEntry = {
    if (fieldValue == newFieldValue)
      copy(candidate = None)
    else
      copy(candidate = Option(newFieldValue))
  }

  def setCandidate(formValue: String): FieldEntry = {
    val simpleFieldValue = fieldValue.asInstanceOf[SimpleFieldValue]
    val updatedFieldValue: SimpleFieldValue = simpleFieldValue.update(formValue)

    if (updatedFieldValue == fieldValue) {
      copy(candidate = None)
    }
    else {
      copy(candidate = Option(updatedFieldValue))
    }
  }


  def acceptCandidate(): FieldEntry = copy(
    candidate = None,
    fieldValue = candidate.getOrElse(throw new IllegalStateException(s"No candidate to accept!")))

  val param: String = fieldKey.param
  override val prompt: String = fieldDefinition.tooltip

  def toCommands: Seq[String] = {
    candidate
      .getOrElse(throw new IllegalStateException(s"No candidate for: $fieldKey!"))
      .toCommands(this)
  }


  def toHtml: String = {
    value.toHtmlField(this)
  }

  def toCell: Cell = {
    Cell.rawHtml(s"$toHtml")
  }

  /**
   *
   * @param macroKey of interest.
   * @return FieldEntry that invokes the macroKey.
   */
  def canTriggerMacro(macroKey: MacroKey): Option[FieldEntry] = {
    fieldValue match {
      case tn: TriggerNode =>
        if (tn.canRunMacro(macroKey))
          Option(this)
        else
          None
      case _ =>
        None
    }
  }


  override def toString: String = {
    s"${fieldKey.fieldName}: ${fieldValue.display}"
  }

  def toRow(maybeRowHeader: Option[Cell] = None): Row = {
    val change = candidate match {
      case Some(c) =>
        Cell(s"${fieldValue.display} => ${c.display}")
      case None => Cell("")
    }
    val row = Row(
      fieldKey.toCell,
      value.toString,
      change
    )
    maybeRowHeader match {
      case Some(header) =>
        row.prepended(header)
      case None =>
        row
    }
  }

  def toJson: FieldEntryJson = {
    FieldEntryJson(this)
  }

  override def compare(that: FieldEntry): Int = fieldKey compare that.fieldKey

  override val template: String = fieldDefinition.template
}


object FieldEntry {

  def apply(complexExtractor: ComplexExtractor, complexFieldValue: ComplexFieldValue[_]): FieldEntry = {

    new FieldEntry(complexExtractor, complexFieldValue.fieldKey, complexFieldValue)
  }

  def header(keyKind: KeyKind): Header = Header(s"${keyKind.name()}", "Number", "Field",
    Cell("Value")
      .withToolTip("Either the candidate or current value."),
    Cell("Change")
      .withToolTip("Shows how the current value will becomes the candidate.")
  )

  def header(count: Int): Header = Header(s"Fields ($count)", "FieldName", "Key", "Value")

}

