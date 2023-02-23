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
import KeyFormats._


import scala.util.matching.Regex

/**
 *
 * @param kind   e.g. port, schedule, macro.
 * @param number 1 to N
 */
sealed abstract class Key(val kind: String, val number: Int) extends CellProvider with Ordered[Key] {
  val index: Int = number - 1

  override def toString: String = s"$kind$number"

  override def toCell: Cell = Cell(toString).withCssClass(kind)

  override def compare(that: Key): Int = {
    var ret = kind compareTo(that.kind)
    if (ret == 0)
      ret = number compareTo that.number
    ret
  }

}

case class PortKey(override val number: Int) extends Key("port", number) {
  assert(number <= 3, "Port numbers are 1 through 3")
}

case class AlarmKey(override val number: Int) extends Key("alarm", number) {
  assert(number <= 5, "Alarm numbers are 1 through 5")
}

case class MacroKey(override val number: Int) extends Key("macro", number) {
  assert(number <= 105, s"Macro numbers are 1 through 105, can't do $number")
}


case class MessageMacroKey(override val number: Int) extends Key("messageMacro", number) {
  assert(number <= 90, "MessageMacro numbers are 1 through 70")
}

case class FunctionKey(override val number: Int) extends Key("function", number) {
  assert(number <= 1005, "Function numbers are 1 through 1005 ")
}

case class ScheduleKey(override val number: Int) extends Key("schedule", number) {
  assert(number <= 40, "Schedule numbers are 1 through 40")
}

case class WordKey(override val number: Int) extends Key("word", number) {
  assert(number <= 255, "Words numbers are 0 through 255")
}

case class DtmfMacroKey(override val number: Int) extends Key("dtmfMacro", number) {
  //  assert(index <= 255, "Words numbers are 0 through 255")
}

case class MiscKey() extends Key("misc", 0) {
}


