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

package net.wa9nnn.rc210.data.datastore

import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.named.NamedKey
import play.api.libs.json.{Format, JsValue, Json}
import net.wa9nnn.rc210.key.KeyFormats.*


/**
 * Data transfer object for JSON.
 * This is what's written to or Parsed (by PlayJson) from the [[DataStore]] JSON data..
 *
 * @param fieldKey   ID of the entry.
 * @param fieldValue current.
 * @param candidate  next value.
 */
case class FieldEntryJson(fieldKey: FieldKey, fieldValue: JsValue, candidate: Option[JsValue])



/**
 * Helper to transfer values from an html form to the [[DataStore]].
 *
 * @param sFieldKey  <input name=??> from a form.
 * @param sFormValue PUT value from the submitted form.
 */
case class FormValue(sFieldKey: String, sFormValue: String) {
  val fieldKey: FieldKey = FieldKey.fromParam(sFieldKey)
}

object FieldEntryJson {
  def apply(fieldEntry: FieldEntry): FieldEntryJson = {
    val fieldKey = fieldEntry.fieldKey
    new FieldEntryJson(fieldKey, fieldEntry.fieldValue.toJsonValue, fieldEntry.candidate.map(_.toJsonValue))
  }


  implicit val fmtFieldEntryJson: Format[FieldEntryJson] = Json.format[FieldEntryJson]

}

/**
 * What is sent to the [[DataStore]] to be the new candidate and name.
 *
 * @param fieldKey   id of value.
 * @param candidate  contents of value. To set as candidate.
 */
case class UpdateCandidate(fieldKey: FieldKey, candidate: Either[String, ComplexFieldValue])

object UpdateCandidate {
  def apply(complexFieldValue: ComplexFieldValue): UpdateCandidate = {
    new UpdateCandidate(complexFieldValue.fieldKey, Right(complexFieldValue))
  }
}

//case class UpdateData(candidates: Seq[UpdateCandidate], names: Seq[NamedKey] = Seq.empty)

case class NamedDataJson(key: Key, name: String)

object NamedDataJson {
  implicit val fmtNamedDataJson: Format[NamedDataJson] = Json.format[NamedDataJson]
}
