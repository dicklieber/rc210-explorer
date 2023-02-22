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

package net.wa9nnn.rc210.data.duration

import net.wa9nnn.rc210.fixtures.WithMemory
import play.api.libs.json.{JsValue, Json, OFormat}
import net.wa9nnn.rc210.data.duration.RcDurationFormats._
class RCDurationSpec extends WithMemory {

  "RCDurationSpec" should {
    "duration" in {

      case class Things(i: Int, seconds: Seconds, minutes:Minutes, tenthSeconds: TenthSeconds, milliseconds: Milliseconds)
      implicit val fmtThings: OFormat[Things] = Json.format[Things]

      val things = Things(123, Seconds("12"), Minutes("5"), TenthSeconds(1), Milliseconds(1000))
      val json: JsValue = Json.toJson(things)


      val sThingJson: String = json.toString

      sThingJson must beEqualTo("""{
                                  |  "i" : 123,
                                  |  "seconds" : "PT12S",
                                  |  "minutes" : "PT5M",
                                  |  "tenthSeconds" : "PT0.1S",
                                  |  "milliseconds" : "PT1S"
                                  |}""".stripMargin)

      val th: Things = Json.parse(sThingJson).as[Things]
      th must beEqualTo (th)
    }
  }
}
