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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.UiType.UiType
import net.wa9nnn.rc210.key.{KeyFormats, KeyKind}
import net.wa9nnn.rc210.serial.Memory

import scala.util.Try

/**
 * //todo move offset to beginning.
 * @param fieldName
 * @param kind
 * @param offset
 * @param extractor
 * @param template
 * @param uiType
 * @param maybeOptions
 */
case class FieldDefinition(fieldName: String, kind: KeyKind, offset: Int, extractor: FieldExtractor, template: String, uiInfo: UiInfo = UiInfo.default) {

  def apply(memory: Memory): Seq[(FieldMetadata, Try[String])] = {

    var start = offset
    for {
      n <- 1 to kind.getMaxN
    } yield {
      val fieldKey = FieldKey(fieldName, KeyFormats.buildKey(kind, n))
      //      val start = offset + bytesPreField * (n - 1)
      val triedValue = Try {
        val er: ExtractResult = extractor(memory, start)
        start = er.newOffset
        er.value
      }
      FieldMetadata(fieldKey, template, uiType, None) -> triedValue
    }
  }

}

