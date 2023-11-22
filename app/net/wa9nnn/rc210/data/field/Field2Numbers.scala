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
import net.wa9nnn.rc210.Key
import play.api.libs.json._
import views.html.fieldString

case class Field2Numbers(value: Seq[Int]) extends SimpleFieldValue {

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey
    val key: Key = fieldKey.key
    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", value.map(int => f"$int%03d").mkString(""))
    )
  }
  

  override def display: String = value.map(_.toString).mkString(" ")

  override def update(paramValue: String): Field2Numbers = {
    val candidate: Seq[Int] = paramValue.split(" ")
      .map(_.toInt)
      .toSeq
    copy(value = candidate)
  }

  override def toJsonValue: JsValue = Json.toJson(this)

}

object Field2Numbers extends SimpleExtractor {
  implicit val fmtField2Numbers: Format[Field2Numbers] = new Format[Field2Numbers] {
    override def writes(o: Field2Numbers) = JsString(o.value.mkString(" "))

    override def reads(json: JsValue): JsResult[Field2Numbers] =

      JsSuccess(Field2Numbers(json.as[String]
        .split(" ")
        .toIndexedSeq
        .map(_.toInt)
      )
      )
  }

  //  override def jsonToField(jsValue: JsValue): FieldValue = jsValue.as[Field2Numbers]

  override val name: String = "Field2Numbers"

  override def extractFromInts(iterator: Iterator[Int], fieldDefinition: SimpleField): FieldValue = {
    Field2Numbers(Seq(iterator.next(), iterator.next()))
  }

  override def parse(json: JsValue): FieldValue =
    json.as[Field2Numbers]

}

