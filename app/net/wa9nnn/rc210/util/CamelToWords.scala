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

import scala.util.matching.Regex

object CamelToWords {

  // Thanks to https://www.baeldung.com/java-camel-case-title-case-to-words
  private val regex: Regex = """(([A-Z]?[a-z]+)|([A-Z\d]))""".r

  /**
   * Convert camel-case to words
   *
   * @param text i.e GuestMacroRange
   * @return i.e. "GuestMacroRange"
   *
   */
  def apply(text: String): String = {
    regex.findAllMatchIn(text).map { (m: Regex.Match) =>
      m.group(0)
    }.toSeq.mkString(" ")
  }
}
