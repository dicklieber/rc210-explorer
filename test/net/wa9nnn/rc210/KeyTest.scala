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

import play.api.libs.json.{Format, Json}

class KeyTest extends RcSpec {
  private val macroKey3 = Key(KeyMetadata.Macro, 3)
  "toString" when
    {
      "just metadata" in
        {
          val string = macroKey3.toString
          string mustBe "Macro3"
        }
      "all parts" in
        {
          val string = Key(KeyMetadata.Port, 3, "color").toString
          string mustBe "Port3:color"
        }
    }
  "id" when
    {
      "all parts" in
        {
          val key = Key(KeyMetadata.Port, 3, "color")
          val id = key.id
          id mustBe "Port|3|color"
          val backAgain = Key.fromId(id)
          backAgain mustBe key
        }
    }
  "round trip toString get" in
    {
      val string = macroKey3.id
      val backAgain = Key.fromId(string)
      backAgain mustBe macroKey3
    }
  "round trip JSON" in
    {
      val sJson = Json.prettyPrint(Json.toJson(macroKey3))
      sJson mustBe """"Macro|3|"""".stripMargin
      val backAgain = Json.parse(sJson).as[Key]
      backAgain mustBe macroKey3
    }
  "throw if too big a number" in
    {
      assertThrows[AssertionError] { // Result type: Assertion
        Key(KeyMetadata.Meter, 42)
      }
    }
  "macroKeys" in
    {
      val keys = Key.macroKeys
      keys must have length KeyMetadata.Macro.maxN
    }
  "path binder" when
    {
      val key = Key(KeyMetadata.Port, 3, "beep")
      val binder = Key.keyKindPathBinder
      "binds" in
        {
          val value1: Either[String, Key] = binder.bind("p", "Macro|3|")
          value1 mustBe Right(macroKey3)
        }
      "unbind" in
        {
          val r: String = binder.unbind("p", key)
          r mustBe "Port|3|beep"
        }
    }
  "ordering" in
    {
      val unordered = Seq(
        macroKey3,
        Key(KeyMetadata.Port, 3, "color"),
        Key(KeyMetadata.Port, 1, "color"),
        Key(KeyMetadata.Port, 2, "color"),
        Key(KeyMetadata.Port, 2, "alpha"),
        Key(KeyMetadata.Port, 2, "beep"),
        Key(KeyMetadata.Clock),
        Key(KeyMetadata.Port, 2, "omega")
      )
      val sorted = unordered.sorted
      sorted.head mustBe Key(KeyMetadata.Clock)
      val last = sorted.last
      last.toString mustBe "Port3:color"
    }
  "replaceN" in {
    val key = Key(KeyMetadata.Port, 3, "color")
    val str = key.replaceN("|>>>>n<<=|")
    str mustBe "|>>>>3<<=|"
  }
}


