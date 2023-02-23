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

package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.{Key, KeyFormats}

import scala.util.matching.Regex


case class FieldKey(name: String, key: Option[Key]) {
  /**
   * can identify this in a HTTP param.
   */
  val param: String = s"$name${key.map(":" + _.toString).getOrElse("")}"
}

object FieldKey {

  def apply(fieldName: String): FieldKey = {
    new FieldKey(fieldName, None)
  }

  def apply(fieldName: String, key: Key): FieldKey = {
    new FieldKey(fieldName, Option(key))
  }

  def fromParam(param: String): FieldKey = {
    val xx: Iterator[Regex.Match] = r.findAllMatchIn(param)
    val m: Regex.Match = xx.next()
    val n = m.group(1)
    val k = m.group(2)
    FieldKey(n, Option(k).map(KeyFormats.parseString))
  }

  private val r = """([a-zA-Z]+):?(:?(.+))?""".r

}

/**
 *
 * @param name    this is key within a [[MappedValues]].
 * @param command to be sent to an RC-210.
 */
case class FieldMetadata(name: String, command: String)

case class ParsedValue(fieldKey: FieldKey, value: String)
