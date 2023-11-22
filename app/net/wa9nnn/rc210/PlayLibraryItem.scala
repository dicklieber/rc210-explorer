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

package net.wa9nnn.rc210

import enumeratum.*
//import enumeratum.values._

sealed abstract class PlayLibraryItem(val value: Int, val name: String) extends EnumEntry

case object PlayLibraryItem extends PlayEnum[PlayLibraryItem] {

  // A good mix of named, unnamed, named + unordered args
  case object Book     extends PlayLibraryItem(value = 1, name = "book")
  case object Movie    extends PlayLibraryItem(name = "movie", value = 2)
  case object Magazine extends PlayLibraryItem(3, "magazine")
  case object CD       extends PlayLibraryItem(4, name = "cd")

  val values = findValues

}

//import play.api.libs.json.{ JsNumber, JsString, Json => PlayJson }
//PlayLibraryItem.values.foreach { item =>
//  assert(PlayJson.toJson(item) == JsNumber(item.value))
//}