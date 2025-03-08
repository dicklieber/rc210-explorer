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
import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.ui.FormField
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue, Json}

case class FieldDtmf(value: String) extends FieldValueSimple() with LazyLogging:
  logger.debug("value: {}", value)

  override def toRow(fieldEntry: FieldEntry): Row = Row(
    fieldEntry.fieldDefinition.fieldName,
    value
  )
  override def toEditCell(fieldKey: Key): Cell = FormField(fieldKey, value)

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(key: Key, template: String): String =
    key.replaceN(template)
      .replaceAll("v", value) //todo probably not right.

  override def displayCell: Cell = Cell(value)

case class DefDtmf(offset: Int, fieldName: String, keyMetadata: KeyMetadata, template: String, maxDigits: Int)
  extends FieldDefSimple[FieldDtmf]:

  override def extract(iterator: Iterator[Int]): FieldDtmf =
    val tt: Array[Char] = iterator.takeWhile(_ != 0)
      .map(_.toChar).toArray
    val str: String = new String(tt)
    FieldDtmf(str)

  override def fromString(str: String): FieldDtmf =
    FieldDtmf(str)

  val fmt: Format[FieldDtmf] = new Format[FieldDtmf] {
    override def reads(json: JsValue): JsResult[FieldDtmf] = JsSuccess(new FieldDtmf(json.as[String]))

    override def writes(o: FieldDtmf): JsValue = JsString(o.value)
  }

