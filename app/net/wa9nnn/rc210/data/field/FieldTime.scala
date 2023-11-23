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

import com.fasterxml.jackson.datatype.jsr310.deser.JSR310DateTimeDeserializerBase
import com.wa9nnn.util.tableui.Cell
import net.wa9nnn.rc210.Key
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue, Json}

import java.time.LocalTime

case class FieldTime(value: LocalTime = LocalTime.MIN) extends SimpleFieldValue {

  def toHtmlField(renderMetadata: RenderMetadata): String = {
    views.html.fieldTime(value, renderMetadata).toString()
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey
    val key:Key = fieldKey.key
    val minute = value.getMinute
    val hour = value.getHour
    val timePiece = f"$hour%02d*$minute%02d"
    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", timePiece))

  }


  override def display: String = value.toString

  override def update(paramValue: String): FieldTime = {
    val candidate = LocalTime.parse(paramValue)
    copy(value = candidate)
  }

  override def toJsonValue: JsValue = Json.toJson(this)

}

object FieldTime extends SimpleExtractor {
  val time = "Time"

  def apply()(implicit nameToValue: Map[String, String]): FieldTime = {
    val sTime = nameToValue(time)
    val localTime = LocalTime.parse(sTime)
    new FieldTime(localTime)
  }

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): FieldValue = throw new IllegalStateException("Can't exrtract as rc2input!")

  implicit val fmtFieldTime: Format[FieldTime] = new Format[FieldTime] {
    override def reads(json: JsValue): JsResult[FieldTime] = JsSuccess(new FieldTime())

    override def writes(o: FieldTime): JsValue = JsString(o.value.toString)
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[FieldTime]

  override val name: String = "FieldTime"

//  override def fromForm(name: String)(implicit kv: Map[String, String],key: Key): LocalTime = {
//    LocalTime.parse(formValue(name))
//  }
}