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
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue, Json}
import views.html.flowChartButton

case class FieldMacroKey(key: Key) extends FieldValueSimple(key):
  def update(formFieldValue: String): FieldValueSimple =
    val key = Key(formFieldValue)
    FieldMacroKey(key)

  override def tableSection(fieldKey: FieldKey): TableSection =
    TableSectionButtons(fieldKey, Row("Macro", fieldKey.key.keyWithName))

  override def displayCell: Cell = Cell(key.keyWithName)

  def toCommands(fieldEntry: FieldEntryBase): Seq[String] = Seq.empty //todo

  def toJsValue: JsValue = Json.toJson(key)

  override def toEditCell(fieldKey: FieldKey): Cell = FormField(fieldKey, key)

  override def toRow: Row = Row(
    "FieldMacroKey", toString
  )

object MacroKeyExtractor extends SimpleExtractor:
  override def extractFromInts(iterator: Iterator[Int], fieldDefinition: FieldDefinitionSimple): FieldValue = {
    val i: Int = iterator.next()
    val key = Key(KeyKind.Macro, i)
    FieldMacroKey(key)
  }

  val name: String = "MacroKey"

  implicit val fmtFieldMacroKey: Format[FieldMacroKey] = new Format[FieldMacroKey] {
    override def reads(json: JsValue): JsResult[FieldMacroKey] = {
      val key = json.as[Key]
      JsSuccess(FieldMacroKey(key))
    }

    override def writes(o: FieldMacroKey): JsValue = Json.toJson(o.key)
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[FieldMacroKey]
