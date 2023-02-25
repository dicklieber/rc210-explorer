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
 * Metadata about a key kind,
 */
object KeyKinds extends Enumeration {
  /**
   *
   * @param maxN how many numbers this key can have.
   * @param name also used as cssClass when for rendering keys of this kinnd.
   */
   case class KeyKind(maxN: Int, name: String) extends super.Val {
  }

  val alarmKey: KeyKind = KeyKind(5, "alarm")
  val dtmfMacroKey: KeyKind = KeyKind(5, "dtmfMacro")
  val functionKey: KeyKind = KeyKind(1005, "function")
  val macroKey: KeyKind = KeyKind(105, "macro")
  val messageMacroKey: KeyKind = KeyKind(90, "messageMacro")
  val miscKey: KeyKind = KeyKind(1, "misc")
  val portKey: KeyKind = KeyKind(3, "port")
  val scheduleKey: KeyKind = KeyKind(40, "schedule")
  val wordKey: KeyKind = KeyKind(256, "word")

}
