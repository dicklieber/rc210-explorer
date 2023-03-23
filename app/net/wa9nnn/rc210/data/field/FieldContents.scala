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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.KeyFactory.Key
import play.api.libs.json._
import views.html._

/**
 * Holds the value for a field.
 * Knows how to render as HTML control or string for JSON, showing to a user or RC-210 Command,
 * Has enough metadata needed yo render
 */
trait FieldContents {

  def toJsValue: JsValue

  def display: String

  /**
   * Render this value as an RD-210 command string.
   */
  def toCommand(fieldEntry: FieldEntry): String

  /**
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   * @param fieldEntry all the metadata.
   * @return html
   */

  def toHtmlField(fieldEntry: FieldEntry): String

  //  def toCommand(fieldKey: FieldKey, commandTemplate: String): String = {
  //
  //    val map: Map[String, () => String] = Seq(
  //      "v" -> (() => commandStringValue),
  //      "b" -> (() => commandBoolDigit),
  //      "n" -> (() => fieldKey.key.number.toString),
  //      "S" -> (() => commandStringValue.toCharArray.mkString(" ")) // dtmf digits space seperated.
  //    ).toMap
  //
  //    map.foldLeft(commandTemplate) { (command: String, tr) =>
  //      val str: String = command.replaceAll(tr._1, tr._2())
  //      str
  //    }
  //    //todo color token and replacement parts <span> s
  //  }

}

trait FieldWithFieldKey[K <: Key] extends FieldContents {
  val key: K
  val fieldName: String
  lazy val fieldkey: FieldKey = FieldKey(fieldName, key)
}

// simple field are defined here. More complex ones like [[net.wa9nnn.rc210.data.schedules.Schedule]] are elsewhere.
case class FieldInt(value: Int) extends FieldContents {
  override def toJsValue: JsValue = JsNumber(BigDecimal.int2bigDecimal(value))

  /**
   * Render as HTML for this field.
   * This is typically just one form field for [[SimpleFieldExtractor]] fields.
   * For complex fields like [[net.wa9nnn.rc210.data.schedules.Schedule]] it's an entire HTML form.
   *
   * @return
   */
  def toHtmlField(fieldEntry: FieldEntry): String = {
    fieldNumber(fieldEntry.param, value).toString()
  }

  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def display: String = value.toString
}


case class FieldDtmf(value: String) extends FieldContents {
  override def toJsValue: JsValue = JsString(value)

  def toHtmlField(fieldEntry: FieldEntry): String = {
    fieldDtmf(fieldEntry.param, value).toString()
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def display: String = value
}

case class FieldBoolean(value: Boolean) extends FieldContents {
  override def toJsValue: JsValue = JsBoolean(value)

  override def toHtmlField(fieldEntry: FieldEntry): String = {
    fieldCheckbox(fieldEntry.param, value).toString()
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def display: String = value.toString
}

case class FieldSeqInts(value: Int*) extends FieldContents {
  override def toJsValue: JsValue = {
    JsArray(value.map((int: Int) => JsNumber(BigDecimal.int2bigDecimal(int))))
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???


  override def toHtmlField(fieldEntry: FieldEntry): String = {
    fieldString(fieldEntry.param, toString).toString()
  }

  override def display: String = value.map(_.toString).mkString(" ")
}

case class FieldSelect(value: Int) extends FieldContents {
  override def toJsValue: JsValue = Json.toJson(value)

  override def toHtmlField(fieldEntry: FieldEntry): String = {
    val value1: FieldContents = fieldEntry.fieldValue
    val options: Seq[SelectOption] = fieldEntry.fieldDefinition.uiInfo.options()

    views.html.fieldSelect(
      paramId = fieldEntry.param,
      options = fieldEntry.fieldDefinition.uiInfo.options()).toString()
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def display: String = value.toString
}