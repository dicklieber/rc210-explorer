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

import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

class FieldEntrySpec extends Specification with DataTables {

  "FieldEntry" should {
    "command" >> {
      "Index" || "Value" | "Command" |
        0 ! "ABC" ! "*2108ABC" |
        1 ! "A2345" ! "*2093A2345" |
        2 ! "true" ! "*51041" |
        2 ! "false" ! "*51040" |
        3 ! "42" ! "2*1000142" |
        8 ! "false" ! "2110" |
        8 ! "true" ! "2111" |> { (fdIndex, value, expectedCommand: String) =>

        val metadata = FieldDefinitions.fields(fdIndex)
        val fieldKey = metadata.fieldKey(2)
        val fieldEntry = FieldEntry(FieldValue(fieldKey, value), metadata)
        val command = fieldEntry.command
        command must beEqualTo(expectedCommand)
      }
    }


  }
}
