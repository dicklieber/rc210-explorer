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
import net.wa9nnn.rc210.serial.Slice
import play.api.libs.json._
import play.twirl.api.Html
import views.html._

/**
 * Holds the value for a field.
 * Knows how to render as HTML control or string for JSON, showing to a user or RC-210 Command,
 */
trait FieldContents {
  def toJsValue: JsValue
  val slice:Slice

  def commandBoolDigit: String = throw new IllegalStateException(" Not a boolean!")

  val commandStringValue: String

  def toHtmlField(fieldKey: FieldKey, uiInfo: UiInfo): Html

  def toCommand(fieldKey: FieldKey, commandTemplate: String): String = {

    val map: Map[String, () => String] = Seq(
      "v" -> (() => commandStringValue),
      "b" -> (() => commandBoolDigit),
      "n" -> (() => fieldKey.key.number.toString),
      "S" -> (() => commandStringValue.toCharArray.mkString(" ")) // dtmf digits space seperated.
    ).toMap

    map.foldLeft(commandTemplate) { (command: String, tr) =>
      val str: String = command.replaceAll(tr._1, tr._2())
      str
    }
    //todo color token and replacement parts <span> s
  }

  override def toString: String = s"$commandStringValue"
}

// simple field are defined here. More complex ones like [[net.wa9nnn.rc210.data.schedules.Schedule]] are elsewhere.
case class FieldInt(slice:Slice, value: Int) extends FieldContents {
  override def toJsValue: JsValue = JsNumber(BigDecimal.int2bigDecimal(value))

  override def toHtmlField(fieldKey: FieldKey, uiInfo: UiInfo): Html = {
    fieldNumber(fieldKey.param, value)
  }

  override val commandStringValue: String = value.toString

  override def commandBoolDigit: String = throw new IllegalStateException("Not a boolean!")

}

case class FieldDtmf(slice:Slice,value: String) extends FieldContents {
  override def toJsValue: JsValue = JsString(value)

  override def toHtmlField(fieldKey: FieldKey, uiInfo: UiInfo): Html = {
    fieldDtmf(fieldKey.param, value)
  }

  override val commandStringValue: String = value
}

case class FieldBoolean(slice:Slice,value: Boolean) extends FieldContents {
  override def toJsValue: JsValue = JsBoolean(value)

  override def toHtmlField(fieldKey: FieldKey, uiInfo: UiInfo): Html =
    fieldCheckbox(fieldKey.param, value)

  override val commandStringValue: String = {
    if (value) "1" else "0"
  }
}

case class FieldSeqInts(slice:Slice, value: Int*) extends FieldContents {
  override def toJsValue: JsValue = {
    JsArray(value.map((int: Int) => JsNumber(BigDecimal.int2bigDecimal(int))))
  }

  override val commandStringValue: String = toString

  override def toCommand(fieldKey: FieldKey, commandTemplate: String): String = super.toCommand(fieldKey, commandTemplate)


  override def toHtmlField(fieldKey: FieldKey, uiInfo: UiInfo): Html = {
    fieldString(fieldKey.param, toString)
  }

  override def toString: String = value.map(_.toString).mkString(" ")
}

case class FieldSelect(slice:Slice, value: Int ) extends FieldContents {
  override def toJsValue: JsValue = JsNumber(value)

  override def toHtmlField(fieldKey: FieldKey, uiInfo: UiInfo): Html = {
    views.html.fieldSelect(value = value, paramId = fieldKey.param, options = uiInfo.selectOptions)
  }

  override val commandStringValue: String = value.toString

}