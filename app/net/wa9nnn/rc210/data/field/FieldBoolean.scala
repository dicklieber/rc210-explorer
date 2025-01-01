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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row}
import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.ui.FormField
import net.wa9nnn.rc210.ui.nav.{BooleanCell, CheckBoxCell}
import play.api.libs.json.*

case class FieldBoolean(value: Boolean = false) extends FieldValueSimple() :
  override def toRow: Row = Row(
    "FieldBoolean",
    toString
  )

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntry): Seq[String] =
    val key = fieldEntry.key
    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", if (value) "1" else "0")
      .replaceAll("b", if (value) "1" else "0")
    )

  override def displayCell: Cell = BooleanCell(value)


  override def toEditCell(key: Key): Cell =
    FormField(key, value)

case class DefBool(offset: Int, fieldName: String, keyMetadata: KeyMetadata, template: String)
  extends FieldDefSimple[FieldBoolean]:
  override def fromForm(formValue: String): FieldBoolean =
    FieldBoolean(formValue == "on")

  override def extract(iterator: Iterator[Int]): FieldValueSimple =
    FieldBoolean(iterator.next() != 0)

  override def writes(o: FieldBoolean): JsValue =
    JsBoolean(o.value)

  override def reads(json: JsValue): JsResult[FieldBoolean] =
    JsSuccess( FieldBoolean(json.as[Boolean]))




