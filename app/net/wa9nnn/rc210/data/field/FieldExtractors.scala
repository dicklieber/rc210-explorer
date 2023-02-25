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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.Slice

/**
 */
object FieldExtractors extends LazyLogging {
  def apply(exractorName: String, slice: Slice): String = {
    exractorName match {
      case "dtmf" =>
        new String(slice.data
          .takeWhile(_ != 0)
          .map(_.toChar) //todo how about A-D?
          .toArray
        )
      case "bool" =>
        (slice.head > 0).toString
      case "int8" =>
        slice.head.toString

        // more go here
      case x =>
        throw new IllegalArgumentException("Don't have a fieldExtractor named: x!")

    }
  }
}
