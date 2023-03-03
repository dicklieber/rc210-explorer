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

package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.key.MacroKey
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}


class FieldKeySpec extends Specification {

  "FieldKeySpec" should {
    val mk = MacroKey(42)
    val fieldKey = FieldKey("afield", mk)
    "with key" in {
      val param = fieldKey.param

      val backAgain = FieldKey.fromParam(param)
      backAgain must beEqualTo(fieldKey)
    }

    "with space in name" in {
      val fieldKey = FieldKey("Groucho Marx", mk)
      val param = fieldKey.param

      val backAgain = FieldKey.fromParam(param)
      backAgain must beEqualTo(fieldKey)
    }

    "json round trip" in {
      val json: JsValue = Json.toJson(fieldKey)
      val sJson = json.as[String]
      sJson must beEqualTo ("afield|macro42")

/*
      val value1: JsValue = Json.parse(sJson)
      val backAgain = value1.as[FieldKey]
      backAgain must beEqualTo (fieldKey)
*/
    }
  }
}
