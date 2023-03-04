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

/**
 * Metadata for the various [[Key]]s
 */
object KeyKindEnum extends Enumeration {
  /**
   * Provides metadata about the [[Key]] types
   *
   * @param maxN numbered key instances can go from 1 from 1 to maxN,
   * @param instantiate create an instance of the Key for this [[KeyKind]] and number.
   */
  case class KeyKind(maxN: Int, instantiate: Int => Key) extends super.Val {

    def apply[T](number: Int): T = {
      instantiate(number).asInstanceOf[T]
    }
  }

  val alarmKey: KeyKind = KeyKind(5,  AlarmKey)
  val dtmfMacroKey: KeyKind = KeyKind(195, DtmfMacroKey)
  val courtesyToneKey: KeyKind = KeyKind(10, CourtesyToneKey)
  val functionKey: KeyKind = KeyKind(1005, FunctionKey)
  val macroKey: KeyKind = KeyKind(105, MacroKey)
  val messageMacroKey: KeyKind = KeyKind(90, MessageMacroKey)
  val miscKey: KeyKind = KeyKind(1, MiscKey)
  val portKey: KeyKind = KeyKind(3, PortKey)
  val scheduleKey: KeyKind = KeyKind(40, ScheduleKey)
  val wordKey: KeyKind = KeyKind(256, WordKey)

}



