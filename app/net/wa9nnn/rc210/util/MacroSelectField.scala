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
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.field.{FieldValue, RenderMetadata, SimpleExtractor, SimpleField}
import net.wa9nnn.rc210.model.TriggerNode
import play.api.libs.json.*

/**
 */
case class MacroSelectField(value: Key = Key(KeyKind.macroKey, 1)) extends FieldSelect[Key] with TriggerNode {

  override val key: Key = value

  override def toRow: Row = {
    Row(headerCell = value.toCell)
  }

//  override def selectOptions: Seq[SelectOption] = Key[MacroKey](KeyKind.macroKey).map { macroKey =>
//    SelectOption(macroKey.toString, macroKey.keyWithName)
//  }

  override val name: String = MacroSelectField.name

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
   * @return a new [[MacroSelectField]].
   */
  override def update(paramValue: String): MacroSelectField = {
    new MacroSelectField(Key(paramValue))
  }


  override def canRunMacro(macroKey: Key): Boolean = value == macroKey
}

object MacroSelectField extends SimpleExtractor {
  val name: String = "Macro"

  /**
   *
   * @param valueMap from form data for this key
   * @return
   */
  def apply()(implicit valueMap: Map[String, String]): MacroSelectField = {
    val str = valueMap(name)
    val macroKey: Key = Key(str)
    new MacroSelectField(macroKey)
  }

  def apply(number: Int): MacroSelectField = {
    new MacroSelectField(Key(KeyKind.macroKey, number))
  }

  implicit val fmtMacroSelect: Format[MacroSelectField] = new Format[MacroSelectField] {
    override def reads(json: JsValue): JsResult[MacroSelectField] = {
      val macrokey: Key = Key(json.as[String])
      JsSuccess(MacroSelectField(macrokey))
    }

    override def writes(o: MacroSelectField): JsValue = JsString(o.value.toString)
  }

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): MacroSelectField = {
    MacroSelectField(itr.next() + 1)
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[MacroSelectField]

  override def fromForm(name: String)(implicit kv: Map[String, String], key: Key): Key = {
//    KeyFactory(formValue(name))
    throw new NotImplementedError() //todo
  }
}


