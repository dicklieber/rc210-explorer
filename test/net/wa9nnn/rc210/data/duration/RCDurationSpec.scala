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

import io.circe
import io.circe.Json
import io.circe.generic.JsonCodec
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import net.wa9nnn.rc210.fixtures.WithMemory

class RCDurationSpec extends WithMemory {

  "RCDurationSpec" should {
    "duration" in {

      @JsonCodec
      case class Things(i: Int, seconds: Seconds, minutes:Minutes, tenthSeconds: TenthSeconds, milliseconds: Milliseconds)

      val thing = Things(123, Seconds("12"), Minutes("5"), TenthSeconds(1), Milliseconds(1000))
      val thingJson: Json = thing.asJson


      val sThingJson: String = thingJson.toString
      sThingJson must beEqualTo("""{
                                  |  "i" : 123,
                                  |  "seconds" : "PT12S",
                                  |  "minutes" : "PT5M",
                                  |  "tenthSeconds" : "PT0.1S",
                                  |  "milliseconds" : "PT1S"
                                  |}""".stripMargin)
      val thEither: Either[circe.Error, Things] = decode[Things](sThingJson)
      thEither must beRight(thing)
    }
  }
}
