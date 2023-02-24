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

package net.wa9nnn.rc210.data.mapped

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.data.FieldMetadata
import net.wa9nnn.rc210.util.CamelToWords
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter
import play.api.data.{Field, FieldMapping, Form, FormError, ObjectMapping1, WrappedMapping}
import play.api.libs.json.{Json, OFormat}

/**
 * Holds information about a field.
 *
 * @param metadata    immutable stuff that's known about a field.n
 * @param fieldState  what we start with.
 */
case class FieldContainer(val metadata: FieldMetadata, fieldState: FieldState) extends RowSource with Ordered[FieldContainer] {
  val value: String = fieldState.value

  //  private var fieldState: FieldState = FieldState(initialValue)

  /*  def toField: Field = {
      val form = Form[FieldContainer](FieldMapping(key = "xyzzy"))
  Field(form)
    }*/

  def updateCandidate(value: String): FieldContainer = {
    copy(fieldState = fieldState.setCandidate(value))
  }

  def candidate: Option[String] = fieldState.candidate

  def acceptCandidate(): FieldContainer = {
    copy(fieldState = fieldState.acceptCandidate())
  }

  def state: FieldState = fieldState

  override def toRow: Row =
    Row(metadata.fieldKey.key.toCell, metadata.fieldKey.fieldName, fieldState.value, fieldState.candidate, metadata.command)

  override def compare(that: FieldContainer): Int = {
    metadata.fieldKey compareTo (that.metadata.fieldKey)
  }

  def prettyName: String = CamelToWords(metadata.fieldKey.fieldName)
}

object FieldContainer {
  def header(count: Int): Header = Header(s"Mapped Values ($count)", "Key", "Field Name", "Current", "Candidate", "Command")

  def apply(fieldMetadata: FieldMetadata, initialValue: String): FieldContainer = new FieldContainer(fieldMetadata, FieldState(initialValue))

  implicit val fmtFieldState: OFormat[FieldState] = Json.format[FieldState]
  implicit val fmtFieldMetadata: OFormat[FieldMetadata] = Json.format[FieldMetadata]
  implicit val fmtFieldContainer: OFormat[FieldContainer] = Json.format[FieldContainer]

  /*implicit object FieldFormatter extends Formatter[FieldContainer] {
    override val format: Option[(String, Nil.type)] = Some(("Expected Format FieldContriner", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Callsign] = parsing(Callsign(_), "error.callsign", Nil)(key, data)

    override def unbind(key: String, value: Callsign) = Map(key -> value.toString)
  }*/

}
