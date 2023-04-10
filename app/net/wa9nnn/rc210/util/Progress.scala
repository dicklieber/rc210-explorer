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

import play.api.libs.json.{Format, Json}

import java.time.{Duration, Instant}


case class Progress(n: Int, of: Int = 4096, duration: Duration, itemsPerSecond: Long)

object Progress {
  def apply(soFar: Int)(implicit start: Instant): Progress = {
    val duration = Duration.between(start, Instant.now())
    val itemsPerSecond: Long = if (soFar > 0)
      soFar / duration.getSeconds
    else
      0

    new Progress(n = soFar, duration = duration, itemsPerSecond = itemsPerSecond)
  }

  implicit val fmtProgress: Format[Progress] = Json.format[Progress]
}