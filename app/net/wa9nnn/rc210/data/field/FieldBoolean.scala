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

import com.wa9nnn.util.tableui.Cell
import play.api.libs.json.*

case class FieldBoolean(value: Boolean = false) extends SimpleFieldValue {

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey
    val key: Key = fieldKey.key
    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", if (value) "1" else "0")
      .replaceAll("b", if (value) "1" else "0")
    )
  }

  override def display: String = value.toString

//  override def toCell(renderMetadata: RenderMetadata): Cell = {
//    super.toCell(renderMetadata)
//  }

  override def update(paramValue: String): FieldBoolean = {
    FieldBoolean(paramValue == "true")
  }

  override def toJsonValue: JsValue = Json.toJson(this)

}

object FieldBoolean extends SimpleExtractor:

  override def extractFromInts(itr: Iterator[Int], fieldDefinition: SimpleField): FieldValue = FieldBoolean(itr.next() > 0)


  implicit val fmtFieldBoolean: Format[FieldBoolean] = new Format[FieldBoolean] {

    override def writes(o: FieldBoolean) = JsBoolean(o.value)

    override def reads(json: JsValue): JsSuccess[FieldBoolean] = JsSuccess(new FieldBoolean(json.as[Boolean]))
  }


  override def parse(jsValue: JsValue): FieldValue = FieldBoolean(jsValue.as[Boolean])

  override val name: String = "FieldBoolean"

