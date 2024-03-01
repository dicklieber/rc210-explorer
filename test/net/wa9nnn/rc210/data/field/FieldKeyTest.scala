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

import net.wa9nnn.rc210.data.clock.ClockNode
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind, RcSpec}
import play.api.libs.json.Json

class FieldKeyTest extends RcSpec {

  "FieldKey" should {
    "round trip" when {
      "name is key" in {
      val key = Key.portKeys.head
      val fieldKey = FieldKey(key)
      val string = fieldKey.toString
      string mustBe "Port1:Port"
      val backAgain = FieldKey(string)
      backAgain.key mustBe key
      backAgain.fieldName mustBe KeyKind.Port.toString

    }
    "none 0 name is not key " in {
      val key = Key(KeyKind.Port, 2)
      val fname = "fname"
      val fieldKey = FieldKey(fname, key)
      val string = fieldKey.toString
      string mustBe "Port2:fname"
      val backAgain = FieldKey(string)
      backAgain.key mustBe key
      backAgain.fieldName mustBe fname

    }
    "0 rcNumber e.g. Clock, RemoteBase etc." in {
      val key = ClockNode.key
      val fieldKey = FieldKey(key)
      val string = fieldKey.toString
      string mustBe ("Clock")
      val backAgain = FieldKey(string)
      backAgain.key mustBe key
      backAgain.fieldName mustBe "Clock"

    }
  }
    "compare" when {
      "same" in {
        val fk1 = FieldKey("f1", Key.portKeys.head)
        val fk2 = FieldKey("f1", Key(KeyKind.Port, 1))
        fk1 compareTo (fk2) mustBe (0)
      }
    }
    "round trip JSON" when{
      "port" in {
        val fk1 = FieldKey("f1", Key.portKeys.head)
        val string = Json.toJson(fk1).toString
        string mustEqual(""""Port1:f1"""")
        val backAgain = Json.parse(string).as[FieldKey]
        backAgain mustEqual(fk1)
      }
      "Clock" in {
        val fk1 = FieldKey(Key(KeyKind.Clock))
        val string = Json.toJson(fk1).toString
        string mustEqual(""""Clock"""")
        val backAgain = Json.parse(string).as[FieldKey]
        backAgain mustEqual(fk1)
      }

    }
    "Cell" in  {
      val fieldKey = FieldKey(Key(KeyKind.Clock))
      val cell = fieldKey.editButtonCell
      val value1 = cell.value
      value1 mustBe("""<button type="button" class="bi bi-pencil-square btn p-0" onclick="window.location.href='/edit/Clock'">
                      |      </button>
                      |""".stripMargin)
    }
    "opt" in  {
      val fieldKey = FieldKey(Key(KeyKind.Clock))
      val backAgain = FieldKey.opt(Option("Clock"))
      backAgain.get mustEqual(fieldKey)
    }
  }
}
