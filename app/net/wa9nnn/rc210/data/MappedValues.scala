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

import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import net.wa9nnn.rc210.Key
import play.api.data.Field
import play.api.libs.json.{JsArray, JsObject, JsValue, Json, OFormat}

/**
 * Maintains a collection of field values and metadata.
 */
class MappedValues(val key: Key) {
  private val map = new TrieMap[String, FieldContainer]

  def acceptCandidate(fieldName: String): Unit = {
    val container = map(fieldName)
    map.put(fieldName, container.acceptCandidate())
  }


  def container(key: String): FieldContainer = map(key)


  /**
   * Add a new entry.
   *
   * @param value         it's initial value
   * @param fieldMetadata fixed stuff we know about the field.
   */
  def setupField(fieldMetadata: FieldMetadata, initialValue: String): Unit = {
    val fieldName = fieldMetadata.name
    assert(!map.contains(fieldName), s"Map already has a container for key: $fieldName")
    val container = FieldContainer(fieldMetadata, initialValue)
    map.put(fieldName, container)
  }

  /**
   * set a new candidate.
   *
   * @param key
   * @param value
   */
  def update(key: String, value: String): Unit = {
    val container = map.getOrElse(key, throw new IllegalStateException(s"Field for key: $key has not been setup, must invoke setupField first!"))
    map.put(key, container.updateCandidate(value))
  }

  def toJson: JsArray = {
    import FieldContainer._
    import play.api.libs.json._

    Json.arr(
      map.values
    )
  }

}

object MappedValues {
  implicit val fmtMappedValues: OFormat[MappedValues] = new OFormat[MappedValues] {

    import net.wa9nnn.rc210.KeyFormats._

    override def writes(o: MappedValues) = {
      Json.obj(
        "key" -> o.key,
        "values" -> Json.arr(o.map.values)
      )
    }

      override def reads(json: JsValue) = {
        throw new NotImplementedError() //todo
      }
    }

  }

  /**
   * Holds information abiuyt a field.
   *
   * @param metadata    immutable stuff that's known about a field.n
   * @param fieldState  what we start with.
   */
  case class FieldContainer(val metadata: FieldMetadata, fieldState: FieldState) {
    val value: String = fieldState.value

    //  private var fieldState: FieldState = FieldState(initialValue)

    def toField: Field = {
      //todo probably cant make this here.
      throw new NotImplementedError() //todo
    }

    def updateCandidate(value: String): FieldContainer = {
      copy(fieldState = fieldState.setCandidate(value))
    }

    def candidate: Option[String] = fieldState.candidate

    def acceptCandidate(): FieldContainer = {
      copy(fieldState = fieldState.acceptCandidate())
    }

    def state: FieldState = fieldState
  }

  object FieldContainer {
    def apply(fieldMetadata: FieldMetadata, initialValue: String): FieldContainer = new FieldContainer(fieldMetadata, FieldState(initialValue))

    implicit val fmtFieldState: OFormat[FieldState] = Json.format[FieldState]
    implicit val fmtFieldMetadata: OFormat[FieldMetadata] = Json.format[FieldMetadata]
    implicit val fmtFieldContainer: OFormat[FieldContainer] = Json.format[FieldContainer]


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