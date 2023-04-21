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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry}
import net.wa9nnn.rc210.data.named.{NamedKey, NamedSource}
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.key.KeyFormats._
import net.wa9nnn.rc210.key.KeyKind
import play.api.libs.json.Json

import java.nio.file.Files
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.collection.immutable.Seq

/**
 * Holds all values as simple key->value.
 * Until loaded all functions that return Seq[FieldEntry] will return Seq.empty.
 * Also holds named Keys.
 */
@Singleton
class DataStore @Inject()(datFile: DatFile) extends NamedSource with LazyLogging {
  Key.setNamedSource(this)
  private var valuesMap: TrieMap[FieldKey, FieldEntry] = new TrieMap[FieldKey, FieldEntry]
  private val keyNamesMap = new TrieMap[Key, String]

  /**
   * Replaces all fieldEntries in the [[DataStore]].
   */
  def load(fieldEntries: Seq[FieldEntry]): Unit = {
    val map = new TrieMap[FieldKey, FieldEntry]
    fieldEntries.foreach { fieldContents =>
      map.put(fieldContents.fieldKey, fieldContents)
      this.valuesMap = map
    }
  }

  def all: Seq[FieldEntry] = {
    valuesMap.values.toSeq.sorted
  }

  /**
   * @return all the the entries for the [[KeyKind]].
   */
  def apply(keyKind: KeyKind): Seq[FieldEntry] = {
    valuesMap.values.filter(_.fieldKey.key.kind == keyKind)
      .toSeq
      .sortBy(_.fieldKey)
  }

  /**
   *
   * @param fieldKey of interest.
   * @return [[FieldEntry]] if fieldKey was found.
   */
  def apply(fieldKey: FieldKey): Option[FieldEntry] = {
    valuesMap.values.find(_.fieldKey == fieldKey)
  }

  /**
   *
   * @param key of interest
   * @return al the field for the key.
   */
  def apply(key: Key): Seq[FieldEntry] = {
    valuesMap.values
      .filter(_.fieldKey.key == key)
      .toSeq
      .sortBy(_.fieldKey.fieldName)
  }

  def candidates: Seq[FieldEntry] = all.filter(_.candidate.nonEmpty)

  /**
   * Moves the  candidate to the fieldValue
   * This should be invoked after successfully sending the candidate to the RC-210.
   *
   * @param fieldKey of
   * @throws IllegalStateException if there is no candidate.
   */
  def acceptCandidate(fieldKey: FieldKey): Unit = {
    valuesMap.put(fieldKey, valuesMap(fieldKey).acceptCandidate())
    save()
  }

  def update(updateData: UpdateData): Unit = {
    handleCandidates(updateData.candidates)
    handleNames(updateData.names)
    save()
  }

  def update(fieldEntries: Seq[FieldEntry]): Unit = {
    fieldEntries.foreach { fieldEntry =>
      valuesMap.put(fieldEntry.fieldKey, fieldEntry)
    }
    save()
  }

  private def handleCandidates(candidates: Seq[UpdateCandidate]): Unit = {
    candidates.foreach { candidate: UpdateCandidate =>
      val fieldKey = candidate.fieldKey
      val currentEntry = valuesMap(candidate.fieldKey)
      val newEntry: FieldEntry = candidate.candidate match {
        case Left(formValue: String) =>
          currentEntry.setCandidate(formValue)

        case Right(value: ComplexFieldValue[_]) =>
          currentEntry.setCandidate(value)
        case x =>
          logger.error(s"Neither right nor left.  $x")
          throw new NotImplementedError() //todo
      }
      valuesMap.put(fieldKey, newEntry)
    }
  }

  private def handleNames(namedKeys: Seq[NamedKey]): Unit = {
    // Now handle key name.
    namedKeys.foreach { namedKey =>
      val key = namedKey.key
      val name = namedKey.name
      if (name.isBlank)
        keyNamesMap.remove((key))
      else
        keyNamesMap.put(key, name)
    }
  }


  private def save(): Unit = {
    Files.createDirectories(datFile.dataStsorePath.getParent)
    val jsObject = Json.toJson(this)
    val sJson = Json.prettyPrint(jsObject)
    Files.writeString(datFile.dataStsorePath, sJson)
    logger.debug("Saved DataStore to {}", datFile.dataStsorePath)
  }

  override def nameForKey(key: Key): String = {
    keyNamesMap.getOrElse(key, "")
  }

  def allNamedKeys: Seq[NamedDataJson] = keyNamesMap.map { case (key, name) =>
    NamedDataJson(key, name)
  }.toSeq
}

object DataStore {

  import play.api.libs.json._

  implicit val fmtDataStore: Format[DataStore] = new Format[DataStore] {
    override def reads(json: JsValue): JsResult[DataStore] = ???

    override def writes(o: DataStore): JsValue = {
      val jsValues: JsArray = JsArray(o.all.map(fieldEntry => Json.toJson(FieldEntryJson(fieldEntry))))
      val jsNames: JsArray = JsArray(o.allNamedKeys.map(Json.toJson(_)))
      JsObject(Seq(
        "values" -> jsValues,
        "keyNames" -> jsNames
      ))
    }
  }
}

