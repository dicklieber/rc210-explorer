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

import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.data.mapped.FieldContainer
import net.wa9nnn.rc210.fixtures.WithMemory

class DataProviderSpec extends WithMemory {

  "DataProvider" should {
    val rc210Data: Rc210Data = new DataProvider().rc210Data
    "rc210Data" in {
      val triggersForMacro1 = rc210Data.triggers(MacroKey(1))
      val triggersForMacro3 = rc210Data.triggers(MacroKey(3))

      triggersForMacro1 must haveLength(1)
      triggersForMacro3 must haveLength(2)
    }

    "port keys" >> {
      val fieldsForPort: Seq[FieldContainer] = rc210Data.mappedValues.fieldsForKey(PortKey(1))
      fieldsForPort.foreach(container =>
      println(container)
      )
      fieldsForPort must haveLength(26)
    }
    "misc keys" >> {
      val miscFields: Seq[FieldContainer] = rc210Data.mappedValues.fieldsForKey(MiscKey())
      miscFields.foreach(container =>
      println(container)
      )
      miscFields.length must be >= 0
    }

    "Known keys" >> {
      val knownKeys = rc210Data.mappedValues.knownKeys
      knownKeys.foreach{k =>
        println(k)
      }
      pending
    }

  }
}
