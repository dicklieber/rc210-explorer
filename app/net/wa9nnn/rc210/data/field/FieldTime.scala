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

import net.wa9nnn.rc210.data.field
import play.api.libs.json.{JsValue, Json}

import java.time.LocalTime

case class FieldTime(value: LocalTime = LocalTime.MIN) extends FieldValue {
  override def toJsValue: JsValue = Json.toJson(value)

  def toHtmlField(renderMetadata: RenderMetadata): String = {
    views.html.fieldTime(value, renderMetadata).toString()
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def display: String = value.toString

  override def update(paramValue: String): FieldValue = {
    val candidate = LocalTime.parse(paramValue)
    copy(value = candidate)
  }
}

object FieldTime {
  val time = "Time"

  def apply()(implicit nameToValue: Map[String, String]): FieldTime = {
    val sTime = nameToValue(time)
    val localTime = LocalTime.parse(sTime)
    new field.FieldTime(localTime)
  }
}