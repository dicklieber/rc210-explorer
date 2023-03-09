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

import play.api.libs.json._

trait FieldContents{
  def toJsValue:JsValue

  def toCommand(fieldEntry: FieldEntry): String = {
    val number = fieldEntry.fieldKey.key.number.toString
    var  bool: String = "0"
    var dtmf:String = ""
    var int:Int = Int.MinValue

    fieldEntry.fieldValue.contents match {
      case FieldBoolean(value) =>
        bool = if(value) "1" else "0"
      case FieldDtmf(value) =>
        dtmf = value.toCharArray.mkString(" ")
      case FieldInt(v) =>
        int = v
      case _ =>
    }
    val map: Map[String, () => String] = Seq(
      "v" -> (() => int.toString),
      "b" -> (() => bool),
      "n" -> (() => number),
      "S" -> (() => dtmf)  // dtmf digits space seperated.
    ).toMap

    map.foldLeft(fieldEntry.fieldMetadata.template) { (command: String, tr) =>
      val str: String = command.replaceAll(tr._1, tr._2())
      str
    }
    //todo color token and replacement parts <span> s
  }


}
// simple field are defined here. More complex ones like [[net.wa9nnn.rc210.data.schedules.Schedule]] are elsewhere.
case class FieldInt(value: Int) extends FieldContents {
  override def toJsValue: JsValue = JsNumber(BigDecimal.int2bigDecimal(value))
}
case class FieldDtmf(value: String) extends FieldContents {
  override def toJsValue: JsValue = JsString(value)
}
case class FieldBoolean(value: Boolean) extends FieldContents {
  override def toJsValue: JsValue = JsBoolean(value)
}
case class FieldSeqInts(value:Int *) extends FieldContents {
  override def toJsValue: JsValue = {
    JsArray(value.map((int: Int) => JsNumber(BigDecimal.int2bigDecimal(int))))
  }
}