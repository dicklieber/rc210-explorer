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
import net.wa9nnn.rc210.key.KeyKindEnum.portKey
import net.wa9nnn.rc210.key.PortKey
import net.wa9nnn.rc210.serial.Slice
import org.specs2.matcher.DataTables

class FieldExtractorsSpec extends WithMemory with DataTables {
  "int16" >> {
    "In" || "Result" |
      Slice(Seq(32, 3)) ! "800" |
      Slice(Seq(0, 0)) ! "0" |
      Slice(Seq(1, 5)) ! "1281" |> { (slice, result: String) =>
      val fieldContents: FieldInt = FieldExtractors.int16.extract(slice).asInstanceOf[FieldInt]
      println(s"$slice => $fieldContents")
      fieldContents.commandStringValue must beEqualTo(result)
      fieldContents.value.toString must beEqualTo(result)
    }
  }
  "twoint16" >> {
    "In" || "Result" |
      Seq(32, 3, 32, 3) ! "800 800" |
      Seq(88, 2, 0, 0) ! "600 0" |
      Seq(0, 0, 0, 0) ! "0 0" |> { (bytes, result: String) =>
      val slice = Slice(bytes)
      val fieldSeqInts: FieldSeqInts = FieldExtractors.twoInts.extract(slice).asInstanceOf[FieldSeqInts]
      println(s"$slice => $fieldSeqInts")
      fieldSeqInts.commandStringValue must beEqualTo(result)
    }
  }

  "DtmfExtractor" >> {
    "In" || "Result" |
      Seq(65, 66, 67, 0) ! "ABC" |
      Seq(65, 66, 67, 0) ! "ABC" |> { (bytes, result: String) =>
      val slice = Slice(bytes)
      val fieldDtmf: FieldDtmf = DtmfExtractor(3).extract(slice).asInstanceOf[FieldDtmf]
      println(s"$slice => $fieldDtmf")
      fieldDtmf.commandStringValue must beEqualTo(result)
    }
  }

  "UnlockCodes just 1" >> {
    val fieldMetadata = FieldDefinitions.forOffset(76)
    val extractResult: ExtractResult = fieldMetadata.extract(memory, fieldMetadata.offset)
    extractResult.contents.commandStringValue must beEqualTo("72726")
    extractResult.newOffset must beEqualTo(76 + 9)
  }
  "UnlockCodes for all ports" >> {
    val fieldMetadata = FieldDefinitions.forOffset(76)

    var start = fieldMetadata.offset
    val unlockCodes: Seq[FieldContents] = for {
      number <- (1 to portKey.maxN)
    } yield {
      val extractResult: ExtractResult = fieldMetadata.extract(memory, start)
      start = extractResult.newOffset
      extractResult.contents
    }
    unlockCodes must haveLength(3)

    unlockCodes(2) must beAnInstanceOf[FieldDtmf]
  }

}
