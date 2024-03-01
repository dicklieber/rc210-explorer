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
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row}
import net.wa9nnn.rc210.{FieldKey, Key}
import net.wa9nnn.rc210.ui.FormField
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue, Json}

case class FieldDtmf(value: String) extends SimpleFieldValue() with LazyLogging:
  logger.debug("value: {}", value)

  override def toRow: Row = Row(
    "FieldDtmf",
    toString
  )

  override def toEditCell(fieldKey: FieldKey): Cell = FormField(fieldKey, value)

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey
    val key: Key = fieldKey.key
    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", value) //todo probably not right.
    )
  }

  override def displayHtml: String = value

  override def update(paramValue: String): FieldDtmf = {
    FieldDtmf(paramValue)
  }

  override def toJsValue: JsValue = Json.toJson(value)


object FieldDtmf extends SimpleExtractor:

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

