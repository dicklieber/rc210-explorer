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
import play.api.data.Field

/**
 * Maintains a collection of field values and metadata.
 */
class MappedValues {
  def acceptCandidate(fieldName: String): Unit = {
    val container = map(fieldName)
    container.acceptCandidate()
  }

  private val map = new TrieMap[String, FieldContainer]

  def container(key: String): FieldContainer = map(key)


  /**
   * Add a new entry.
   *
   * @param value         it's initial value
   * @param fieldMetadata fixed stuff we know about the field.
   */
  def setupField(fieldName: String, fieldMetadata: FieldMetadata, initialValue:String): Unit = {
    assert(!map.contains(fieldName), s"Map already has a container for key: $fieldName")
    val container = new FieldContainer(fieldMetadata, initialValue)
    map.put(fieldName, container)
  }

  def update(key: String, value: String): Unit = {
    val container = map.getOrElse(key, throw new IllegalStateException(s"Field for key: $key has not been setup, must invoke setupField first!"))
    container.updateCandidate(value)
  }
}

/**
 * Holds information abiuyt a field.
 *
 * @param fieldMetadata immutable stuff that's known about a field.n
 * @param initialValue  what we start with.
 */
class FieldContainer(val fieldMetadata: FieldMetadata, initialValue: String) {
  def value: String = fieldState.value

  private var fieldState: FieldState = FieldState(initialValue)

  def toField: Field = {
    //todo probably cant make this here.
    throw new NotImplementedError() //todo
  }

  def updateCandidate(value: String): Unit = {
    fieldState = fieldState.setCandidate(value)
  }

  def candidate: Option[String] = fieldState.candidate

  def acceptCandidate(): Unit = {
    fieldState = fieldState.acceptCandidate()
  }

  def state: FieldState = fieldState


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