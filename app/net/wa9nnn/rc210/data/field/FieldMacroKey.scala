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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, KvTable, Row, Table, TableSection}
import controllers.{EditController, routes}
import net.wa9nnn.rc210.ui.{FormField, TableSectionButtons}
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.libs.json.{Format, JsObject, JsResult, JsString, JsSuccess, JsValue, Json}
import views.html.flowChartButton

case class FieldMacroKey(key: Key) extends FieldValueSimple(key):

  override def tableSection(key: Key): TableSection =
    TableSectionButtons(key, Row("Macro", key.keyWithName))

  override def displayCell: Cell = Cell(key.keyWithName)

  def toCommands(fieldEntry: FieldEntry): Seq[String] = Seq.empty //todo

  def toJsValue: JsValue = Json.toJson(key)

  override def toEditCell(key: Key): Cell = FormField(key, key)

  override def toRow: Row = Row(
    "FieldMacroKey", toString
  )

  case class DefMacroKey(offset: Int, fieldName: String, keyMetadata: KeyMetadata, override val template: String)
    extends FieldDefSimple[FieldMacroKey]:
    override def fromString(str: String): FieldMacroKey =
      FieldMacroKey(Key.fromId(str))

    override def extract(iterator: Iterator[Int]): FieldMacroKey =
      val key = Key(keyMetadata, iterator.next())
      FieldMacroKey(key)

    override def writes(o: FieldMacroKey): JsValue =
      JsString(o.key.id)

    override def reads(json: JsValue): JsResult[FieldMacroKey] =
      JsSuccess(fromString(json.asInstanceOf[String]))

