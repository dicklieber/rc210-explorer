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

import net.wa9nnn.rc210.ui.FormField
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue, Json}

case class FieldMacroKey(macroKey: Key) extends SimpleFieldValue {
  def update(paramValue: String): SimpleFieldValue =
    val key = Key(paramValue)
    FieldMacroKey(key)

  def displayHtml: String = macroKey.toString

  def toCommands(fieldEntry: FieldEntryBase): Seq[String] = Seq.empty //todo

  def toJsValue: JsValue = Json.toJson(macroKey)

  override def toHtmlField(fieldKey: FieldKey): String = FormField(fieldKey, macroKey)
}

object MacroKeyExtractor extends SimpleExtractor:
  override def extractFromInts(iterator: Iterator[Int], fieldDefinition: SimpleField): FieldValue = {
    val i: Int = iterator.next()
    val key = Key(KeyKind.macroKey, i)
    FieldMacroKey(key)
  }

  val name: String = "MacroKey"

  implicit val fmtFieldMacroKey: Format[FieldMacroKey] = new Format[FieldMacroKey] {
    override def reads(json: JsValue): JsResult[FieldMacroKey] = {
      val key = json.as[Key]
      JsSuccess(FieldMacroKey(key))
    }

    override def writes(o: FieldMacroKey): JsValue = Json.toJson(o.macroKey)
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[FieldMacroKey]
