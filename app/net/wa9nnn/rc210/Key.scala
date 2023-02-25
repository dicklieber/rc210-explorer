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

/**
 *
 * @param kind   e.g. port, schedule, macro.
 * @param number 1 to N
 * @param maxN   how many can we have of this key,
 */
sealed abstract class Key(val kind: String, val number: Int, val maxN: Int) extends CellProvider with Ordered[Key] {

  validate()

  def validate():Unit = assert((1 to maxN).contains(number), s"Key numbers are 1 through $maxN")
  val index: Int = number - 1

  override def toString: String = s"$kind$number"

  override def toCell: Cell = Cell(toString).withCssClass(kind)

  override def compare(that: Key): Int = {
    var ret = kind compareTo that.kind
    if (ret == 0)
      ret = number compareTo that.number
    ret
  }

}

case class PortKey(override val number: Int) extends Key("port", number, 3)

case class AlarmKey(override val number: Int) extends Key("alarm", number, 5)

case class MacroKey(override val number: Int) extends Key("macro", number, 105) {
  assert(number <= maxN, s"Macro numbers are 1 through $maxN, can't do $number")
}


case class MessageMacroKey(override val number: Int) extends Key("messageMacro", number, 90)

case class FunctionKey(override val number: Int) extends Key("function", number, 1005)

case class ScheduleKey(override val number: Int) extends Key("schedule", number, 40)

case class WordKey(override val number: Int) extends Key("word", number, 256)

case class DtmfMacroKey(override val number: Int) extends Key("dtmfMacro", number, 256)

/**
 * There can be any number of [[MiscKey()]] but they don't index into a map by themselves. MaxN just indicate o=how many to extract for a given fieldname.
 */
case class MiscKey() extends Key("misc", 0, 1) {
  override def validate(): Unit = {} // misc is always valid.
}

