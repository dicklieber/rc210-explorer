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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{FieldMetadata, FieldValue}
import net.wa9nnn.rc210.key.Key
import play.api.libs.json.JsArray
import play.api.libs.json.Json
import play.api.libs.json._

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.util.Try

/**
 * Holds most values are simple key->value.
 * Does not include more complex fields e.g.Macro
 */
class MappedValues() {
  private val metadataMap = new TrieMap[FieldKey, FieldMetadata]
  private val valueMap = new TrieMap[FieldKey, FieldValue]

  def allMetadatas: Seq[FieldMetadata] = metadataMap.values.toSeq.sortBy(_.fieldKey)

  /**
   *
   * @param key of interest
   * @return order by field name.
   */
  def fieldsForKey(key: Key): Seq[FieldMetadata] = {
    metadataMap.values
      .filter(_.fieldKey.key == key)
      .toSeq
      .sortBy(_.fieldKey.fieldName)
  }

  def valueForKey(fieldKey: FieldKey): Option[FieldValue] = {
    valueMap.get(fieldKey)
  }

  def knownKeys: Seq[Key] = {
    metadataMap.keys
      .map { fieldKey => fieldKey.key }
      .toSet
      .toSeq
      .sortBy[String](_.toString)
  }

  //  def acceptCandidate(fieldKey: FieldKey): Unit = {
  //    val container = metadataMap(fieldKey)
  //    metadataMap.put(fieldKey, container.acceptCandidate())
  //  }

  /**
   * Add a new entry.
   *
   * @param initialValue         it's initial value
   * @param fieldMetadata        fixed stuff we know about the field.
   */
  def setupField(fieldMetadata: FieldMetadata, initialValue: String): Unit = {
    val fieldKey: FieldKey = fieldMetadata.fieldKey
    assert(!metadataMap.contains(fieldKey), s"Map already has a FieldMetadata for key: $fieldKey")
    metadataMap.put(fieldKey, fieldMetadata)
    valueMap.put(fieldKey, FieldValue(fieldKey, initialValue))
  }

  /**
   * set a new candidate.
   *
   * @param key   of field.
   * @param value new candidate.
   */
  def update(key: FieldKey, value: String): Unit = {
    val fieldValue: FieldValue = valueMap.getOrElse(key, throw new IllegalStateException(s"Field for key: $key has not been setup, must invoke setupField first!"))
    valueMap.put(key, fieldValue.setCandidate(value))
  }

  def toJson: JsArray = {
    import play.api.libs.json._
    Json.arr(
      valueMap.values
    )
  }
}

object MappedValues {
  import play.api.libs.json._

  implicit val fmtMappedValues: OFormat[MappedValues] = new OFormat[MappedValues] {

    override def writes(mappedValues: MappedValues): JsObject = {
       JsObject(
        mappedValues
          .valueMap
          .values
          .toSeq
          .sortBy(_.fieldKey.fieldName)
          .map(fieldValue => fieldValue.fieldKey.param -> JsString(fieldValue.value))
      )
    }

    override def reads(json: JsValue): JsResult[MappedValues] = {
      JsResult.fromTry(Try {
        val mappedValues = new MappedValues()
        val map: mutable.Map[FieldKey, FieldValue] = mappedValues.valueMap
        val containers: Seq[FieldValue] = json.as[Seq[FieldValue]]
        containers.foreach(fieldMetadata =>
          map.put(fieldMetadata.fieldKey, fieldMetadata)
        )
        mappedValues
      }
      )
    }
  }

}


