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
 * All keys live here
 * They are all created at startup.
 * Instances can only be obtained by calling methods on the object.
 */
object KeyFactory {

  private val keys: Seq[Key] = {
    {
      for {
        keyKind <- KeyKind.values().toIndexedSeq
        number <- (1 to keyKind.maxN)
      } yield {
        keyKind match {
          case KeyKind.alarmKey => AlarmKey(number)
          case KeyKind.dtmfMacroKey => DtmfMacroKey(number)
          case KeyKind.courtesyToneKey => CourtesyToneKey(number)
          case KeyKind.functionKey => FunctionKey(number)
          case KeyKind.macroKey => MacroKey(number)
          case KeyKind.messageMacroKey => MessageMacroKey(number)
          case KeyKind.commonKey => CommonKey()
          case KeyKind.wordKey => WordKey(number)
          case KeyKind.portKey => PortKey(number)
          case KeyKind.scheduleKey => ScheduleKey(number)
        }
      }
    }
  }

  private val map: Map[String, Key] = keys.map { k =>
    k.toString -> k }.toMap

  def apply[K <: Key](sKey: String): K = {
    map.getOrElse(sKey, throw new IllegalArgumentException(s"No key for : $sKey!")).asInstanceOf[K]
  }

  def apply[K <: Key](keyKind: KeyKind, number: Int): K = {
    apply(keyKind.skey(number))
  }

  val availableKeys: Seq[Key] = {
    keys
  }

  def apply(keyKind: KeyKind): Seq[Key] = {
    availableKeys
      .filter(_.kind.eq(keyKind))
  }
}