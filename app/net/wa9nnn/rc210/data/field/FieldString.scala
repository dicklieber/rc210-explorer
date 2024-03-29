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
import play.api.libs.json.*

case class FieldString(value: String) extends SimpleFieldValue():

  /**
   * Render as HTML for this rc2input.
   * For complex fields like [[net.wa9nnn.rc210.data.schedules.ScheduleNode]] it's an entire HTML form.
   *
   * @return
   */

  override def toEditCell(fieldKey: FieldKey): Cell =
    FormField(fieldKey, value)

  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey
    val key: Key = fieldKey.key

    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", value))

  }

  override def displayCell: Cell = Cell(value)

  override def update(paramValue: String): FieldString = {
    FieldString(paramValue)
  }

  override def toJsValue: JsValue = JsString(value)

  override def toRow: Row = Row(
    "FieldString", toString
  )

object FieldString extends SimpleExtractor:

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): FieldString = {
    new FieldString(new String(
      itr.takeWhile(_ != 0)
        .toArray
        .map(_.toChar)))
  }

  implicit val fmtStringInt: Format[FieldString] = new Format[FieldString] {
    override def reads(json: JsValue): JsResult[FieldString] = JsSuccess(new FieldString(json.as[String]))

    override def writes(o: FieldString): JsValue = Json.toJson(o.value)
  }

  override def parse(jsValue: JsValue): FieldValue = new FieldInt(jsValue.as[Int])




