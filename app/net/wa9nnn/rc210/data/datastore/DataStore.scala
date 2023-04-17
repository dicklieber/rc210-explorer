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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, SimpleFieldValue}
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.key.KeyKind
import play.api.libs.json.{Format, JsValue, Json}

import java.nio.file.Files
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.collection.immutable.Seq

/**
 * Holds all values as simple key->value.
 * Until loaded all functions that return Seq[FieldEntry] will return Seq.empty.
 */
@Singleton
class DataStore @Inject()(datFile: DatFile) {
  private var map: TrieMap[FieldKey, FieldEntry] = new TrieMap[FieldKey, FieldEntry]

  /**
   * Replaces all fieldEntries in the [[DataStore]].
   */
  def load(fieldEntries: Seq[FieldEntry]): Unit = {
    val map = new TrieMap[FieldKey, FieldEntry]
    fieldEntries.foreach { fieldContents =>
      map.put(fieldContents.fieldKey, fieldContents)
      this.map = map
    }
  }

  /**
   * Updates with supplied fieldEntries.
   * Can't do a load as there may not be enough entries on the json file.
   *
   * @param fieldEntries
   */
  def update(fieldEntries: Seq[FieldEntry]): Unit = {
    fieldEntries.foreach { fe =>
      map.put(fe.fieldKey, fe)
    }
    save()
  }


  def all: Seq[FieldEntry] = {
    map.values.toSeq.sorted
  }


  /**
   * @return all the the entries for the [[KeyKind]].
   */
  def apply(keyKind: KeyKind): Seq[FieldEntry] = {
    map.values.filter(_.fieldKey.key.kind == keyKind)
      .toSeq
      .sortBy(_.fieldKey)
  }

  /**
   *
   * @param fieldKey of interest.
   * @return [[FieldEntry]] if fieldKey was found.
   */
  def apply(fieldKey: FieldKey): Option[FieldEntry] = {
    map.values.find(_.fieldKey == fieldKey)
  }

  /**
   *
   * @param key of interest
   * @return al the field for the key.
   */
  def apply(key: Key): Seq[FieldEntry] = {
    map.values
      .filter(_.fieldKey.key == key)
      .toSeq
      .sortBy(_.fieldKey.fieldName)
  }

  /**
   * Moves the  candidate to the fieldValue
   * This should be invoked after successfully sending the candidate to the RC-210.
   *
   * @param fieldKey of
   * @throws IllegalStateException if there is no candidate.
   */
  def acceptCandidate(fieldKey: FieldKey): Unit = {
    map.put(fieldKey, map(fieldKey).acceptCandidate())
    save()
  }


  private def update(fieldKey: FieldKey, value: String): Unit = {
    val entry: FieldEntry = map(fieldKey)
    map.put(fieldKey, entry.setCandidate(value))
  }

  //  def setCandidate(fieldKey: FieldKey, fieldContents: FieldValue): Unit = {
  //    val entry: FieldEntry = map.getOrElse(fieldKey, throw new IllegalArgumentException(s"No value for $fieldKey"))
  //    map.put(fieldKey, entry.copy(candidate = Option(fieldContents)))
  //    save()
  //  }


  def simpleCandidate(newCandidate: FormValue): Unit =
    simpleCandidate(Seq(newCandidate))

  def simpleCandidate(newValues: Iterable[FormValue]): Unit = {
    newValues.foreach { formValue =>
      val fieldKey = formValue.fieldKey
      val currentEntry: FieldEntry = map.apply(fieldKey)
      val newFieldEntry = currentEntry.setCandidate(formValue.sFormValue)
      map.put(fieldKey, newFieldEntry)
    }
    save()
  }

  def complexCandidate(newValue: ComplexFieldValue[_]): Unit =
    complexCandidate(Seq(newValue))

  def complexCandidate(newValues: Iterable[ComplexFieldValue[_]]): Unit = {
    newValues.foreach { fieldValue: ComplexFieldValue[_] =>
      val fieldKey = fieldValue.fieldKey

      val current: FieldEntry = map.apply(fieldKey)
      val updatedFieldEntry: FieldEntry = current.setCandidate(fieldValue)
      map.put(fieldKey, updatedFieldEntry)
    }
    save()
  }

  //  def apply[K <: Key](fields: Seq[ComplexFieldValue[K]]): Unit = {
  //    fields.foreach { fieldWithFieldKey =>
  //      apply(fieldWithFieldKey.fieldKey, fieldWithFieldKey)
  //    }
  //    save()
  //  }


  private def save(): Unit = {
    Files.createDirectories(datFile.dataStsorePath.getParent)
    val jsObject = Json.toJson(this)
    val sJson = Json.prettyPrint(jsObject)
    Files.writeString(datFile.dataStsorePath, sJson)
  }
}

object DataStore {

  import play.api.libs.json._

  implicit val fmtMappedValues: Format[DataStore] = new Format[DataStore] {
    override def reads(json: JsValue): JsResult[DataStore] = ???

    override def writes(o: DataStore): JsValue = {
      Json.toJson(o.all.map(fieldEntry =>
        Json.toJson(FieldEntryJson(fieldEntry))))
    }
  }
}


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




