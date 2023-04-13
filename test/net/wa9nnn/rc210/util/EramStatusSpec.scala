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

package net.wa9nnn.rc210.util

import net.wa9nnn.rc210.util.EramStatus.expectedInts
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}

import scala.util.parsing.combinator.RegexParsers

class EramStatusSpec extends Specification {

  "EramStatus" should {
    "happy path" in {
      val eramStatus = new EramStatus("com4")
      eramStatus.progress must beEqualTo(Progress(running = true, "0.0%"))
      Thread.sleep(2000)
      eramStatus.update(2000)
      val progress = eramStatus.progress
      progress must beEqualTo(Progress(running = true, "45.5%"))
      val itemsPerSecond = eramStatus.itemsPerSecond
      itemsPerSecond must be ~ (1000 +/- 20)
    }

    "all" >> {
      val eramStatus = new EramStatus("com4")
      for {
        n <- 0 until expectedInts
      } {
        eramStatus.update(n)
        val progress = eramStatus.progress
        val percent = progress.percent
        println(s"n: $n percent: $percent")
      }
      eramStatus.finish()
      val finishProgress = eramStatus.progress
      finishProgress.running must beFalse
      finishProgress.percent must beEqualTo ("100.0%")
    }
  }
}
