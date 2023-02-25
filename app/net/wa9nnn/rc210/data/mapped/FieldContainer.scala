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

import com.wa9nnn.util.tableui.{Cell, Header, Row, RowSource}
import net.wa9nnn.rc210.data.field.FieldMetadata
import net.wa9nnn.rc210.util.CamelToWords
import play.api.libs.json.{Json, OFormat}
import scala.jdk.CollectionConverters._
/**
 * Holds information about a field.
 *
 * @param metadata    immutable stuff that's known about a field.n
 * @param fieldState  what we start with.
 */
case class FieldContainer(val metadata: FieldMetadata, fieldState: FieldState) extends RowSource with Ordered[FieldContainer] {
  val value: String = fieldState.value
  val bool:Boolean = value == "true"

  import org.apache.commons.text.StringSubstitutor
  lazy val map: Map[String, String] = Seq(
    "value" -> value,
    "bool" -> (if(bool) "1" else "0"),
    "port" -> metadata.fieldKey.key.number.toString
  ).toMap
  lazy val substitutor = new StringSubstitutor(map.asJava)

  def updateCandidate(value: String): FieldContainer = {
    copy(fieldState = fieldState.setCandidate(value))
  }

  def candidate: Option[String] = fieldState.candidate

  def acceptCandidate(): FieldContainer = {
    copy(fieldState = fieldState.acceptCandidate())
  }

  def state: FieldState = fieldState

  lazy val command:String = {
    //todo color token and replacement parts <span> s
    substitutor.replace(metadata.template)
  }

  override def toRow: Row =
    Row(metadata.fieldKey.key.toCell,
      Cell(CamelToWords(metadata.fieldKey.fieldName))
      .withToolTip(s"Actual field name: ${metadata.fieldKey.fieldName}"),
      fieldState.value,
      fieldState.candidate,
      metadata.template,
      command
    )

  override def compare(that: FieldContainer): Int = {
    metadata.fieldKey compareTo (that.metadata.fieldKey)
  }

  def prettyName: String = CamelToWords(metadata.fieldKey.fieldName)
}

object FieldContainer {
  def header(count: Int): Header = Header(s"Mapped Values ($count)", "Key", "Field Name", "Current", "Candidate", "Template", "Command")

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
