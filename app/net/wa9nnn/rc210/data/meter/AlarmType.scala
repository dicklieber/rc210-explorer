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

package net.wa9nnn.rc210.data.meter

import enumeratum.values.*
import net.wa9nnn.rc210.data.meter.AlarmType.values
import net.wa9nnn.rc210.ui.{Rc210Enum, Rc210EnumEntry}

sealed abstract class AlarmType(val rc210Value: Int) extends Rc210EnumEntry:
  override val vals: Seq[Rc210EnumEntry] = values

case object AlarmType extends Rc210Enum[AlarmType] {
  override val values: IndexedSeq[AlarmType] = findValues

  case object LowAlarm extends AlarmType(1)

  case object HighAlarm extends AlarmType(2)
}