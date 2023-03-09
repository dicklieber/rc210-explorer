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

package net.wa9nnn.rc210.key

import com.wa9nnn.util.tableui.{Cell, CellProvider}
import net.wa9nnn.rc210.key.KeyKindEnum._

/**
 * Most things in this program are identified by a [[Key]].
 * Each kind of item has a specific type of Key as declared below.
 *
 * All have a swtring form e.g. "port2 these are uwsed in JSON for of a key etc.
 * @param kind   e.g. port, schedule, macro.
 * @param number 1 to maxN in [[KeyKind]]
 */
sealed abstract class Key(val kind: KeyKind, val number: Int) extends CellProvider with Ordered[Key] {

  val name: String = kind.prettyName

  override val toString: String = s"$name$number"

  override def toCell: Cell = Cell(toString)
    .withCssClass(kind.toString)

  override def compare(that: Key): Int = {
    var ret = kind compareTo that.kind
    if (ret == 0)
      ret = number compareTo that.number
    ret
  }
}

case class PortKey(override val number: Int) extends Key(portKey, number)

case class AlarmKey(override val number: Int) extends Key(alarmKey, number)

case class MacroKey(override val number: Int) extends Key(macroKey, number)

case class MessageMacroKey(override val number: Int) extends Key(messageMacroKey, number)

case class FunctionKey(override val number: Int) extends Key(functionKey, number)
case class ScheduleKey(override val number: Int) extends Key(scheduleKey, number)

case class WordKey(override val number: Int) extends Key(wordKey, number)

case class DtmfMacroKey(override val number: Int) extends Key(dtmfMacroKey, number)

case class CourtesyToneKey(override val number: Int) extends Key(dtmfMacroKey, number)

/**
 * There can be any number of [[CommonKey]] but they don't index into a map by themselves. MaxN just indicate o=how many to extract for a given fieldname.
 */
case class CommonKey(override val number: Int = 1) extends Key(commonKey, number)

