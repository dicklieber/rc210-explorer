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

import play.api.libs.json._
import views.html.fieldNumber

case class FieldInt(value: Int) extends SimpleFieldValue {

  /**
   * Render as HTML for this field.
   * For complex fields like [[net.wa9nnn.rc210.data.schedules.Schedule]] it's an entire HTML form.
   *
   * @return
   */
  def toHtmlField(renderMetadata: RenderMetadata): String = {
    fieldNumber(value, renderMetadata).toString()
  }


  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def display: String = value.toString

  override def update(paramValue: String): FieldValue = {
    FieldInt(paramValue.toInt)
  }
  override def toJsonValue: JsValue = Json.toJson(this)

}

object FieldInt extends FieldExtractor {

  override def extract(itr: Iterator[Int], field: SimpleField): FieldInt = {
    new FieldInt(if (field.max > 256)
      itr.next() + itr.next() * 256
    else
      itr.next()
    )
  }

  implicit val fmtFieldInt: Format[FieldInt] = new Format[FieldInt] {
    override def reads(json: JsValue): JsResult[FieldInt] = JsSuccess(new FieldInt(json.as[Int]))

    override def writes(o: FieldInt): JsValue = Json.toJson(o.value)
  }

  override def jsonToField(jsValue: JsValue): FieldValue =  new FieldInt(jsValue.as[Int])
}