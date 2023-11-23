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

package net.wa9nnn.rc210.data.named

import net.wa9nnn.rc210.Key
import play.api.libs.json.{Format, Json}

case class NamedKey(key: Key, name: String) extends Ordered[NamedKey] {
  override def compare(that: NamedKey): Int = key compareTo that.key
}

object NamedKey {
  implicit val fmtNamedKey: Format[NamedKey] = Json.format[NamedKey]
}

trait NamedKeySource {
  def nameForKey(key: Key): String
}


