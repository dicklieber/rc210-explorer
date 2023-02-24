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

import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.FieldMetadata
import play.api.libs.json.{JsArray, JsResult, JsValue, Json, OFormat}

import scala.util.Try

/**
 * Holds most values are simple key->value.
 * Does not include more complex fields e.g.Macro
 */
class MappedValues() {
  private val fieldMap = new TrieMap[FieldKey, FieldContainer]

  def dump: Seq[FieldContainer] = {
    fieldMap.values
      .toSeq
      .sorted
  }

  def container(fieldKey: FieldKey): FieldContainer = fieldMap(fieldKey)

  /**
   *
   * @param key of interest
   * @return order by field name.
   */
  def fieldsForKey(key: Key): Seq[FieldContainer] = {
    fieldMap.values
      .filter(_.metadata.fieldKey.key == key)
      .toSeq
      .sortBy(_.metadata.fieldKey.fieldName)
  }

  def knownKeys: Seq[Key] = {
    fieldMap.keys
      .map { fieldKey => fieldKey.key }
      .toSet
      .toSeq
      .sortBy[String](_.toString)
  }

  def acceptCandidate(fieldKey: FieldKey): Unit = {
    val container = fieldMap(fieldKey)
    fieldMap.put(fieldKey, container.acceptCandidate())
  }

  /**
   * Add a new entry.
   *
   * @param initialValue         it's initial value
   * @param fieldMetadata        fixed stuff we know about the field.
   */
  def setupField(fieldMetadata: FieldMetadata, initialValue: String): Unit = {
    val fieldName = fieldMetadata.fieldKey
    assert(!fieldMap.contains(fieldName), s"Map already has a container for key: $fieldName")
    val container = FieldContainer(fieldMetadata, initialValue)
    fieldMap.put(fieldName, container)
  }

  /**
   * set a new candidate.
   *
   * @param key   of field.
   * @param value new candidate.
   */
  def update(key: FieldKey, value: String): Unit = {
    val container = fieldMap.getOrElse(key, throw new IllegalStateException(s"Field for key: $key has not been setup, must invoke setupField first!"))
    fieldMap.put(key, container.updateCandidate(value))
  }

  def toJson: JsArray = {
    import FieldContainer._
    import play.api.libs.json._

    Json.arr(
      fieldMap.values
    )
  }
}

object MappedValues {
  implicit val fmtMappedValues: OFormat[MappedValues] = new OFormat[MappedValues] {

    override def writes(o: MappedValues) = {
      Json.obj(
        "values" -> Json.arr(o.fieldMap.values)
      )
    }

    override def reads(json: JsValue): JsResult[MappedValues] = {
      JsResult.fromTry(Try {
        val mappedValues = new MappedValues()
        val map = mappedValues.fieldMap
        val containers: Seq[FieldContainer] = json.as[Seq[FieldContainer]]
        containers.foreach(fieldContainer =>
          map.put(fieldContainer.metadata.fieldKey, fieldContainer)
        )
        mappedValues
      }
      )
    }
  }

}


/**
 * Mutable
 *
 * @param value current value.
 */
case class FieldState(value: String, candidate: Option[String] = None) {

  def setCandidate(value: String): FieldState = copy(candidate = Option(value))

  def acceptCandidate(): FieldState = {
    assert(candidate.nonEmpty, "No candidate to accept!")
    FieldState(candidate.get)
  }

  def isDirty: Boolean = candidate.nonEmpty
}