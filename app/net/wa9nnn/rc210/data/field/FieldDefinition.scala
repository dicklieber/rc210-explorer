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

import net.wa9nnn.rc210.KeyFormats
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

import scala.util.Try

case class FieldDefinition(fieldName: String, kind: String, offset: Int, bytesPreField: Int, extractorName: String, template: String, howMany: Int = 1) {
  def apply(memory: Memory): Seq[(FieldMetadata, Try[String])] = {
    for {
      n <- 1 to howMany
    } yield {
      val fieldKey = FieldKey(fieldName, KeyFormats.buildKey(kind, n))
      val slice: Slice = memory(SlicePos(offset + ((n - 1) * bytesPreField)))

      val triedValue = Try {
        FieldExtractors(extractorName, slice)
      }
      FieldMetadata(fieldKey, template) -> triedValue
    }
  }

}

case class FieldMetadata(fieldKey: FieldKey, template: String) {

  def apply(value: String): String =
    template.replace("$", value)

}