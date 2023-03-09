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
import net.wa9nnn.rc210.data.ValuesStore.{ParamValue, ParamValues}
import net.wa9nnn.rc210.data.field.{FieldContents, FieldEntry, FieldMetadata, FieldValue}
import net.wa9nnn.rc210.key.Key
import play.api.libs.json.JsArray

import scala.collection.concurrent.TrieMap

/**
 * Holds most values are simple key->value.
 * Does not include more complex fields e.g.Macro
 */
class MappedValues(fieldEntries: Seq[FieldEntry]) {
  private val metadataMap = new TrieMap[FieldKey, FieldMetadata]
  private val valueMap = new TrieMap[FieldKey, FieldValue]

  fieldEntries.foreach { fieldEntry =>
    val fieldKey = fieldEntry.fieldKey
    metadataMap.put(fieldKey, fieldEntry.fieldMetadata)
    valueMap.put(fieldKey, fieldEntry.fieldValue)
  }

  def all: Seq[FieldEntry] = {
    metadataMap.iterator.map { case (fieldKey: FieldKey, fieldMetadata: FieldMetadata) =>
      val fieldValue = valueMap(fieldKey)
      FieldEntry(fieldValue, fieldMetadata)
    }.toSeq.sorted
  }

  def entity(fieldKey: FieldKey): Option[FieldEntry] = {
    for {
      fieldMetadata <- metadataMap.get(fieldKey)
    } yield {
      val fieldValue = valueMap(fieldKey)
      FieldEntry(fieldValue, fieldMetadata)
    }
  }

  /**
   *
   * @param key of interest
   * @return order by field name.
   */
  def fieldsForKey(key: Key): Seq[FieldEntry] = {
    all.iterator
      .filter(_.fieldKey.key == key)
      .toSeq
      .sortBy(_.fieldKey.fieldName)
  }

  def valueForKey(fieldKey: FieldKey): Option[FieldValue] = {
    valueMap.get(fieldKey)
  }

  //    def knownKeys: Seq[Key] = {
  //      metadataMap.keys
  //        .map { fieldKey => fieldKey.key }
  //        .toSet
  //        .toSeq
  //        .sortBy[String](_.toString)
  //    }

  def acceptCandidate(fieldKey: FieldKey): Unit = {
    valueMap.put(fieldKey, valueMap(fieldKey).acceptCandidate())
  }

  //    /**
  //     * Add a new entry.
  //     *
  //     * @param initialValue         it's initial value
  //     * @param fieldMetadata        fixed stuff we know about the field.
  //     */
  //    def setupField(fieldKey: FieldKey, fieldMetadata: FieldMetadata, initialValue: String): Unit = {
  //
  //      assert(!metadataMap.contains(fieldKey), s"Map already has a FieldMetadata for key: $fieldKey")
  //      assert(!valueMap.contains(fieldKey), s"Map already has a FieldValue for key: $fieldKey")
  //      metadataMap.put(fieldKey, fieldMetadata)
  //      valueMap.put(fieldKey, FieldValue(fieldKey, initialValue))
  //    }

  /**
   * set a new candidate.
   *
   * @param key      of field.
   * @param contents new candidate.
   */
  def update(key: FieldKey, contents: FieldContents): Unit = {
    val fieldValue: FieldValue = valueMap.getOrElse(key, throw new IllegalStateException(s"Field for key: $key has not been setup, must invoke setupField first!"))
    valueMap.put(key, fieldValue.setCandidate(fieldValue.contents))
  }

  def update(setValues: ParamValues): Unit =
    setValues.values
      .foreach { paramValue: ParamValue =>

        //todo convert param to contents base on metadata
        throw new NotImplementedError() //todo
//        update(paramValue.fieldKey, paramValue.contents)
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
          .map(fieldValue => fieldValue.fieldKey.param ->  fieldValue.contents.toJsValue)
      )
    }

    override def reads(json: JsValue): JsResult[MappedValues] = {
      throw new NotImplementedError() //todo
      //      JsResult.fromTry(Try {
      //        val values: Seq[FieldValue] = json.as[Seq[FieldValue]]
      //        values.foreach(fieldMetadata =>
      //          map.put(fieldMetadata.fieldKey, fieldMetadata)
      //        )
      //        mappedValues
      //      }
      //      )
    }
  }

}



