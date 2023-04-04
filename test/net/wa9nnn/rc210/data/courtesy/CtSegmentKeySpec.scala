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

package net.wa9nnn.rc210.data.courtesy

import net.wa9nnn.rc210.key.KeyFactory.CourtesyToneKey
import org.specs2.mutable.Specification

class CtSegmentKeySpec extends Specification {

  "CtSegmentKey" should {
    val csk =  CtSegmentKey("groucho", 2)(CourtesyToneKey(3))
    "rount trip" in {
      val param = csk.param
      param must beEqualTo ("groucho.2.courtesyToneKey3")

      val backAgain = CtSegmentKey(param)
      backAgain.segment must beEqualTo (2)
      backAgain.name must beEqualTo ("groucho")
      backAgain.ctKey.toString must beEqualTo ("courtesyToneKey3")
    }
  }
}
