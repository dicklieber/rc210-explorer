/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */

package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.{KeyIndicator, RcSpec}
import org.scalatest.matchers.must.Matchers.mustBe

class FormDataTest extends RcSpec {

  "FormDataTest" should
    {
      val map: Map[String, Seq[String]] = Map(
        "plain" -> Seq("p1", "p2"),
        "bear" -> Seq("b1"),
        "|Macro|1|dtmf" -> Seq("1234567890*#"),
        "kPort|1|name" -> Seq("Port 1"),
        "|Port|1|color" -> Seq("red"),
        "nPort|1|" -> Seq("ignore")
      )
      val formData: FormData = new FormData(map)
      "bindable" when
        {
          val bindable: Map[String, String] = formData.bindable
          "bear" in
            {
              bindable("bear") mustBe "b1"
            }
          "plain" in
            {
              bindable("plain") mustBe "p1"
            }
          "size" in
            {
              bindable.size mustBe 2
            }
        }

      "maybeKey" in
        {
          val key = formData.maybeKey.value
          key.qualifier.value mustBe "name"
          key.name mustBe "???"
          key.indicator mustBe KeyIndicator.iValue
        }

      "value" in
        {
          formData.value("color") mustBe "red"
        }
    }
}