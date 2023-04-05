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

import net.wa9nnn.rc210.data.field.{FieldExtractor, RenderMetadata, SimpleField}
import net.wa9nnn.rc210.key.KeyFactory.MacroKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.libs.json._

/**
 */
case class MacroSelect(value: MacroKey = KeyFactory.defaultMacroKey) extends FieldSelect[MacroKey] {
  override val selectOptions: Seq[SelectOption] = KeyFactory[MacroKey](KeyKind.macroKey).map { macroKey =>
    SelectOption(macroKey.number, macroKey.toString)
  }
  override val name: String = MacroSelect.name

  /**
   *
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   * @param renderMetadata metadata needed  to render html etc
   * @return html
   */
  override def toHtmlField(renderMetadata: RenderMetadata): String = {
    super.toHtmlField(renderMetadata)
  }

  override def update(paramValue: String): MacroSelect = {
    new MacroSelect(KeyFactory(paramValue))
  }

  def fromParam(param: String): MacroKey =
    KeyFactory(param)
}

object MacroSelect extends FieldExtractor[MacroSelect] {
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

   val options: Seq[SelectOption] = KeyFactory[MacroKey](KeyKind.macroKey).map { macroKey =>
    SelectOption(macroKey.number, macroKey.toString)
  }

  override def extract(itr: Iterator[Int], field: SimpleField): MacroSelect = {
    MacroSelect(itr.next())
  }
}


