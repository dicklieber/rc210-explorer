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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Link}
import net.wa9nnn.rc210.{FieldKey, Key, KeyMetadata, RcSpec}
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import play.api.libs.json.Json

class FieldKeyToString extends RcSpec with TableDrivenPropertyChecks:
  val fieldKeyDisplay: TableFor2[FieldKey, String] =
    Table(
      ("FieldKey", "string"), // First tuple defines column names
      (FieldKey(Key.clockKey), "Clock"),
      (FieldKey(Key.portKeys.head, "Color of port"), "Port1: Color of port"), // Subsequent tuples define the data
      (FieldKey(Key.commonkey, "some fieldName"), "some fieldName"),
    )

  forAll(fieldKeyDisplay) { (fieldKey: FieldKey, string: String) =>
    println(fieldKey)
    println(string)
    val display = fieldKey.display
    display must be(string)
  }
  val fieldKeyStrings: TableFor2[FieldKey, String] =
    Table(
      ("FieldKey", "string"), // First tuple defines column names
      (FieldKey(Key.portKeys.head, "Color of port"), "Port1: Color of port"), // Subsequent tuples define the data
      (FieldKey(Key.clockKey), "Clock"),
      (FieldKey(Key.commonkey, "some fieldName"), "some fieldName"),
    )

  forAll(fieldKeyStrings) { (kk: FieldKey, string: String) =>
    println(kk)
    println(string)
    val display = kk.display
    display must be(string)
  }

class FieldKeyTest extends RcSpec {

  "FieldKey" should {
    val clock: FieldKey = FieldKey(Key.clockKey)

    val commonKey: Key = Key(KeyMetadata.Common)
    val aCommon: FieldKey = FieldKey(commonKey, "aCommonfield")

    val logicAlarm: FieldKey = FieldKey(Key(KeyMetadata.LogicAlarm))
    "clock" in {
      clock.display mustBe("Clock")
    }

    "compare" when {
      "same" in {
        val fk1 = FieldKey(Key.portKeys.head, "f1")
        val fk2 = FieldKey(Key(KeyMetadata.Port, 1), "f1")
        fk1 compareTo (fk2) mustBe (0)
      }
    }
    "id round trip for id" when {
      "Port" in {
        val fieldKey = FieldKey(Key.portKeys.head, "f1")
        val id = fieldKey.id
        id mustBe ("$Port1$f1")
        val backAgain = FieldKey.fromId(id)
        backAgain mustBe (fieldKey)
      }
      "Clock" in {
        val fieldKey = FieldKey(Key.clockKey)
        val id = fieldKey.id
        id mustBe ("$Clock0$")
        val backAgain = FieldKey.fromId(id)
        backAgain mustBe (fieldKey)
        val display = backAgain.display
        display mustBe ("Clock")
      }
      "Common" in {
        val fieldKey = FieldKey(Key.commonkey, "f1")
        val id = fieldKey.id
        id mustBe ("$Common0$f1")
        val backAgain = FieldKey.fromId(id)
        backAgain mustBe (fieldKey)
        backAgain.display mustBe ("f1")
      }
      "Courtesy Tone" in {
        val key1 = Key(KeyMetadata.CourtesyTone, 3)
        val fieldKey = FieldKey(key1)
        val id = fieldKey.id
        id mustBe ("$Courtesy Tone3$")
        val backAgain = FieldKey.fromId(id)
        backAgain mustBe (fieldKey)
        val string = backAgain.toString
        string mustBe ("FieldKey(Courtesy Tone3,)")
        val display = backAgain.display
        display mustBe ("Courtesy Tone3")
      }
    }
    "round trip JSON" in {
      val key = Key.portKeys.head
      val fk1 = FieldKey(key, "f1")
      val string = Json.toJson(fk1).toString
      string mustEqual (""""$Port1$f1"""")
      fk1.key mustEqual  (key)
      fk1.fieldName mustEqual("f1")

      val backAgain = Json.parse(string).as[FieldKey]
      backAgain mustEqual (fk1)
    }
    "tocell" in {
      val cell: Cell = FieldKey(Key.portKeys.head, "f1").editButtonCell
      val value: String = cell.value
      value mustBe("""<a href="/edit/$Port1$f1" class="bi {icon}} btn bi-pencil-square btn-sm p-0" title="Edit this field."></a>""".stripMargin)
    }
  }
}

