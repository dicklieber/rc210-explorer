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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.key.KeyFactory
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue, Json}
import views.html.fieldDtmf

case class FieldDtmf(value: String) extends SimpleFieldValue with LazyLogging{
  logger.debug("value: {}", value)

  def toHtmlField(renderMetadata: RenderMetadata): String = {
    fieldDtmf(value, renderMetadata).toString()
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntryBase): String = {
    val fieldKey = fieldEntry.fieldKey
    val key: KeyFactory.Key = fieldKey.key
    key.replaceN(fieldEntry.template)
      .replaceAll("v", value) //todo probably not right.
  }


  override def display: String = value

  override def update(paramValue: String): FieldDtmf = {
    FieldDtmf(paramValue)
  }

  override def toJsonValue: JsValue = Json.toJson(value)

}

object FieldDtmf extends SimpleExtractor {

  override def extractFromInts(itr: Iterator[Int], fieldDefinition: SimpleField): FieldValue = {
    val ints: Seq[Int] = for {
      _ <- 0 to fieldDefinition.max
    } yield {
      itr.next()
    }

    val tt: Array[Char] = ints.takeWhile(_ != 0)
      .map(_.toChar).toArray
    val str: String = new String(tt)
    new FieldDtmf(str)
  }


  implicit val fmtFieldDtmf: Format[FieldDtmf] = new Format[FieldDtmf] {
    override def reads(json: JsValue): JsResult[FieldDtmf] = JsSuccess(new FieldDtmf(json.as[String]))

    override def writes(o: FieldDtmf): JsValue = JsString(o.value)
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[FieldDtmf]

  override val name: String = "FieldDtmf"
}