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
import play.api.libs.json.{Format, JsResult, JsValue}

/**
 *
 * @param contents current value.
 */
case class FieldValue(fieldKey: FieldKey, contents: FieldContents, candidate: Option[FieldContents] = None) {

  def cssClass: String = if(dirty) "dirtyValue" else ""

  def current: FieldContents = candidate.getOrElse(contents)

//  def bool: Boolean = value == "true"

  def setCandidate(value: FieldContents): FieldValue = copy(candidate = Option(value))

  def acceptCandidate(): FieldValue = {
    assert(candidate.nonEmpty, "No candidate to accept!")
    copy(contents = candidate.get, candidate = None)
  }

  def dirty: Boolean = candidate.nonEmpty

  override def toString: String = s"${fieldKey.param} contents: ${contents.toString} ${contents.getClass}"
}

object FieldValue {
  implicit val fmtFieldValue:
    Format[FieldValue] = new Format[FieldValue]{
    override def writes(o: FieldValue): JsValue = o.contents.toJsValue

    override def reads(json: JsValue): JsResult[FieldValue] = {

      throw new NotImplementedError() //todo
    }
  }


}

