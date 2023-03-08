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

package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.ValuesStore.ValueStoreMessage
import play.api.libs.json.{Json, OFormat}

/**
 *
 * @param value current value.
 */
case class FieldValue(fieldKey: FieldKey, value: String, candidate: Option[String] = None) extends ValueStoreMessage{

  def cssClass: String = if(dirty) "dirtyValue" else ""

  def current: String = candidate.getOrElse(value)

  def bool: Boolean = value == "true"

  def setCandidate(value: String): FieldValue = copy(candidate = Option(value))

  def acceptCandidate(): FieldValue = {
    assert(candidate.nonEmpty, "No candidate to accept!")
    copy(value = candidate.get, candidate = None)
  }

  def dirty: Boolean = candidate.nonEmpty
}

object FieldValue {
  implicit val fmtFieldValue: OFormat[FieldValue] = Json.format[FieldValue]


}