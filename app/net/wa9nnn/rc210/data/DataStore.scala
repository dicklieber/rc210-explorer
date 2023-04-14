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

package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldValue}
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.key.KeyKind
import play.api.libs.json.{Format, JsValue, Json}

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap

/**
 * Holds all values as simple key->value.
 * Until loaded all functions that return Seq[FieldEntry] will return Seq.empty.
 */
@Singleton
class DataStore @Inject()() {
  private var map: TrieMap[FieldKey, FieldEntry] = new TrieMap[FieldKey, FieldEntry]

  /**
   * This will be invoked:
   * - at startup from reading an existing memory file
   * - after re-downloading from an RC-210
   * - after load saved json data.
   */
  def load(fieldEntries: Seq[FieldEntry]): Unit = {
    val map = new TrieMap[FieldKey, FieldEntry]
    fieldEntries.foreach { fieldContents =>
      map.put(fieldContents.fieldKey, fieldContents)

      this.map = map
    }
  }

  def all: Seq[FieldEntry] = {
    map.values.toSeq.sorted
  }


  def apply(keyKind: KeyKind): Seq[FieldEntry] = {
    map.values.filter(_.fieldKey.key.kind == keyKind)
      .toSeq
      .sortBy(_.fieldKey)
  }

  def apply(fieldKey: FieldKey): Option[FieldEntry] = {
    map.values.find(_.fieldKey == fieldKey)
  }

  /**
   *
   * @param key of interest
   * @return order by field name.
   */
  def apply(key: Key): Seq[FieldEntry] = {
    map.values
      .filter(_.fieldKey.key == key)
      .toSeq
      .sortBy(_.fieldKey.fieldName)
  }

  def acceptCandidate(fieldKey: FieldKey): Unit = {
    map.put(fieldKey, map(fieldKey).acceptCandidate())
  }


  def apply(fieldKey: FieldKey, value: String): Unit = {
    val entry: FieldEntry = map(fieldKey)
    map.put(fieldKey, entry.setCandidate(value))
  }

  def apply(fieldKey: FieldKey, fieldContents: FieldValue): Unit = {
    val entry: FieldEntry = map.getOrElse(fieldKey, throw new IllegalArgumentException(s"No value for $fieldKey"))
    map.put(fieldKey, entry.copy(candidate = Option(fieldContents)))
  }

  def apply(newCandidate: NewCandidate): Unit = {
    val fieldKey = newCandidate.fieldKey
    val fieldEntry: FieldEntry = map.apply(fieldKey)
    val updatedFieldEntry: FieldEntry = fieldEntry.setCandidate(newCandidate.formValue)
    map.put(fieldKey, updatedFieldEntry)
  }

  def apply(newValues: Iterable[NewCandidate]): Unit = {
    newValues.foreach(newCandidate => apply(newCandidate))
  }

  def apply[K <: Key](fields: Seq[ComplexFieldValue[K]]): Unit = {
    fields.foreach { fieldWithFieldKey =>
      apply(fieldWithFieldKey.fieldKey, fieldWithFieldKey)
    }
  }

}

object DataStore {

  import play.api.libs.json._

  implicit val fmtMappedValues: Format[DataStore] = new Format[DataStore] {
    override def reads(json: JsValue): JsResult[DataStore] = ???

    override def writes(o: DataStore): JsValue = {

      val value: Seq[(String, JsValue)] = o.all.map { fieldEntry =>
        val fieldEntryJson = FieldEntryJson(fieldEntry)
        fieldEntry.fieldKey.param -> Json.toJson(fieldEntryJson)
      }
      JsObject {
        value
      }
    }
  }
}


case class NewCandidate(fieldKey: FieldKey, formValue: String)

/**
 * Data transfer object for JSON.
 * This is what's written to or Parsed (by PlayJson) from the [[DataStore]] JSON data..
 *
 * @param fieldKey ID
 * @param fieldValue
 * @param candidate
 */
case class FieldEntryJson(fieldKey: FieldKey, fieldValue: JsValue, candidate: Option[JsValue])

object FieldEntryJson {
  def apply(fieldEntry: FieldEntry): FieldEntryJson = {
    val fieldKey = fieldEntry.fieldKey
    new FieldEntryJson(fieldKey, fieldEntry.fieldValue.toJsonValue, fieldEntry.candidate.map(_.toJsonValue))
  }

  implicit val fmtFieldEntryJson: Format[FieldEntryJson] = Json.format[FieldEntryJson]

}




