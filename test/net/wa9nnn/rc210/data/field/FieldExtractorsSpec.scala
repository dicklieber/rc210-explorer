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

import net.wa9nnn.rc210.fixtures.WithMemory
import net.wa9nnn.rc210.serial.Slice
import org.specs2.matcher.DataTables

class FieldExtractorsSpec extends WithMemory with DataTables {
  "int16" >> {
    "In" || "Result" |
      Slice(Seq(32, 3)) ! "800" |
      Slice(Seq(0, 0)) ! "0" |
      Slice(Seq(1, 5)) ! "1281" |> { (slice, result: String) =>
      val str = FieldExtractors.int16.extract(slice)
      println(s"$slice => $str")
      str must beEqualTo(result)
    }
  }
  "twoint16" >> {
    "In" || "Result" |
      Seq(32, 3, 32, 3) ! "800 800" |
      Seq(88, 2, 0, 0 ) ! "600 0" |
      Seq(0,0,0,0) ! "0 0" |> { (bytes, result: String) =>
      val slice = Slice(bytes)
      val str = FieldExtractors.twoint16.extract(slice)
      println(s"$slice => $str")
      str must beEqualTo(result)
    }
  }
}
