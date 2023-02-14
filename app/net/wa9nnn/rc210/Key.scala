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

package net.wa9nnn.rc210

import com.wa9nnn.util.tableui.{Cell, CellProvider}
import play.api.libs.json._

import scala.util.matching.Regex

/**
 *
 * @param kind  e.g. port, schedule, macro.
 * @param index 1 to N
 */
sealed abstract class Key(val kind: String, val index: Int) extends CellProvider{
  override def toString: String = s"$kind$index"

  override def toCell: Cell = Cell(toString).withCssClass(kind)

}

case class PortKey(override val index: Int) extends Key("port", index) {
  assert(index <= 3, "Port numbers are 1 through 3")
}

case class AlarmKey(override val index: Int) extends Key("alarm", index) {
  assert(index <= 5, "Alarm numbers are 1 through 5")
}

case class MacroKey(override val index: Int) extends Key("macro", index) {
  assert(index <= 105, s"Macro numbers are 1 through 105, can't do $index")
}

case class MessageMacroKey(override val index: Int) extends Key("messageMacro", index) {
  assert(index <= 90, "MessageMacro numbers are 1 through 70")
}

case class FunctionKey(override val index: Int) extends Key("function", index) {
  assert(index <= 1005, "Function numbers are 1 through 1005 ")
}

case class ScheduleKey(override val index: Int) extends Key("schedule", index) {
  assert(index <= 40, "Schedule numbers are 1 through 40")
}
case class WordKey(override val index: Int) extends Key("word", index) {
  assert(index <= 255, "Words numbers are 0 through 255")
}


object Key {
  val r: Regex = """([a-zA-Z]+)(\d+)""".r

  def apply(kind: String, number: Int): Key = {
    kind match {
      case "port" =>
        PortKey(number)
      case "alarm" =>
        AlarmKey(number)
      case "macro" =>
        MacroKey(number)
      case "messageMacro" =>
        MessageMacroKey(number)
      case "function" =>
        FunctionKey(number)
      case "word" =>
        WordKey(number)
    }
  }

  implicit val fmtMacroKey: OFormat[MacroKey] = Json.format[MacroKey]
  implicit val fmtMessageMacroKey: OFormat[MessageMacroKey] = Json.format[MessageMacroKey]
  implicit val fmtFunctionKey: OFormat[FunctionKey] = Json.format[FunctionKey]
  implicit val fmtScheduleKey: OFormat[ScheduleKey] = Json.format[ScheduleKey]
  implicit val fmtWordKey: OFormat[WordKey] = Json.format[WordKey]

  implicit val fmtKey: Format[Key] = new Format[Key] {
    override def reads(json: JsValue): JsResult[Key] = {

      try {
        val r(kind, number) = json.as[String]
        JsSuccess(Key(kind, number.toInt))
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(key: Key): JsValue = {
      JsString(key.toString)
    }
  }

}

case class Named(key:Key, name:String)

object  Named {
  implicit val fmtNamed: OFormat[Named] = Json.format[Named]
}


