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

package net.wa9nnn.rc210.util

import com.wa9nnn.util.tableui.Row
import net.wa9nnn.rc210.data.field.{FieldValue, RenderMetadata, SimpleExtractor, SimpleField}
import net.wa9nnn.rc210.key.KeyFactory.{Key, MacroKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.model.TriggerNode
import play.api.libs.json._

/**
 */
case class MacroSelect(value: MacroKey = KeyFactory.defaultMacroKey) extends FieldSelect[MacroKey] with TriggerNode {

  override val key: KeyFactory.Key = value

  override def toRow: Row = {
    Row(headerCell = value.toCell)
  }

  override def selectOptions: Seq[SelectOption] = KeyFactory[MacroKey](KeyKind.macroKey).map { macroKey =>
    SelectOption(macroKey.toString, macroKey.keyWithName)
  }

  override val name: String = MacroSelect.name

  /**
   *
   * Render as HTML. Either a single rc2input of an entire HTML Form.
   *
   * @param renderMetadata metadata needed  to render html etc
   * @return html
   */
  override def toHtmlField(renderMetadata: RenderMetadata): String = {
    super.toHtmlField(renderMetadata)
  }

  /**
   *
   * @param paramValue from html form.
   * @return a new [[MacroSelect]].
   */
  override def update(paramValue: String): MacroSelect = {
    new MacroSelect(KeyFactory.macroKey(paramValue.toInt))
  }

  def fromParam(param: String): MacroKey =
    KeyFactory.macroKey(param.toInt)

  override def canRunMacro(macroKey: MacroKey): Boolean = value == macroKey
}

object MacroSelect extends SimpleExtractor[MacroKey] {
  val name: String = "Macro"

  /**
   *
   * @param valueMap from form data for this key
   * @return
   */
  def apply()(implicit valueMap: Map[String, String]): MacroSelect = {
    val str = valueMap(name)
    val macroKey: MacroKey = KeyFactory(str)
    new MacroSelect(macroKey)
  }

  def apply(number: Int): MacroSelect = {
    new MacroSelect(KeyFactory(KeyKind.macroKey, number))
  }

  implicit val fmtMacroSelect: Format[MacroSelect] = new Format[MacroSelect] {
    override def reads(json: JsValue): JsResult[MacroSelect] = {
      val macrokey: MacroKey = KeyFactory(json.as[String])
      JsSuccess(MacroSelect(macrokey))
    }

    override def writes(o: MacroSelect): JsValue = JsString(o.value.toString)
  }

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): MacroSelect = {
    MacroSelect(itr.next() + 1)
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[MacroSelect]

  override def fromForm(name: String)(implicit kv: Map[String, String], key: Key): MacroKey = {
    KeyFactory(formValue(name))
  }
}


