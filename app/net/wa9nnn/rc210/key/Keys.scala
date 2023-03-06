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

package net.wa9nnn.rc210.key

import net.wa9nnn.rc210.key.KeyKindEnum.KeyKind

/**
 * All of the possible keys for every [[KeyKind]] and maxNs.
 */
object Keys {
  val availableKeys: Seq[Key] = {
    (for {
      f <- KeyKindEnum.values
      keyKind = f.asInstanceOf[KeyKind]
      number <- 1 to keyKind.maxN
    } yield {
      keyKind.apply[Key](number)
    }).toSeq
  }

  def apply(keyKind: KeyKind) :Seq[Key] = {
    availableKeys
      .filter(_.kind.eq(keyKind))
  }
}
