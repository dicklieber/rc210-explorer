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

import net.wa9nnn.rc210.data.FieldMetadata
import play.api.data.Field
import play.api.libs.json.{Json, OFormat}

/**
 * Holds information about a field.
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
