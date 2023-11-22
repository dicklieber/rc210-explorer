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

import net.wa9nnn.RcSpec
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.KeyKind

import java.time.LocalTime

class FieldTimeSpec extends RcSpec {
val fieldTime = FieldTime(LocalTime.of(10, 15))
  "FieldTime" should {
    "toCommand" in {
      val candidate =  FieldTime(LocalTime.of(10, 25))
      val fieldDefinition = SimpleField(17, "Hang Time 3", KeyKind.portKey, "n*40011*8*0*v*02", FieldInt)
      val fieldKey: FieldKey = fieldDefinition.fieldKey(3)
      val fieldEntry = FieldEntry(fieldDefinition, fieldKey, fieldTime, Option(candidate))
      val command = fieldEntry.toCommands.head
      command should equal ("3*40011*8*0*10*25*02")
    }
  }
}
