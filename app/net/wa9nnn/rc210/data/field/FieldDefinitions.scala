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

import net.wa9nnn.rc210.data.field.FieldExtractors._
import net.wa9nnn.rc210.key.KeyKind._

object FieldDefinitions {
  // template ${value}  ${bool} ${port}
  val fields: Seq[FieldDefinition] = Seq(
    FieldDefinition(fieldName = "SitePrefix", kind = miscKey, offset = 0, bool, template = "*2108${value}"),
    FieldDefinition(fieldName = "TTPadTest", kind = miscKey, offset = 4, extractor = dtmf, template = "*2093${value}"),
    FieldDefinition(fieldName = "SayHours", kind = miscKey, offset = 10, bool, template = "*5104${bool}"),
    FieldDefinition(fieldName = "HangTime1", kind = portKey, offset = 11, extractor = int8, template = "*10001${value}"),
    FieldDefinition(fieldName = "HangTime2", kind = portKey, offset = 14, extractor = int8, template = "*10002${value}"),
    FieldDefinition(fieldName = "HangTime3", kind = portKey, offset = 17, extractor = int8, template = "*10003${value}"),
    FieldDefinition(fieldName = "IIDMinutes", kind = portKey, offset = 20, extractor = int8, template = "*1002${value}"),
    FieldDefinition(fieldName = "PIDMinutes", kind = portKey, offset = 23, extractor = int8, template = "*1002${value}"),
    FieldDefinition(fieldName = "TxEnable", kind = portKey, offset = 26, extractor = bool, template = "${port}111${bool}"),
    FieldDefinition(fieldName = "DTMFCoverTone", kind = portKey, offset = 29, extractor = bool, template = "${port}113${bool}"),
    FieldDefinition(fieldName = "DTMFMuteTimer", kind = portKey, offset = 32, extractor = int16, template = "${port}*1006${bool}"),
    FieldDefinition(fieldName = "Kerchunk", kind = portKey, offset = 38, extractor = bool, template = "${port}*115${bool}"),
    FieldDefinition(fieldName = "KerchunkTimer", kind = portKey, offset = 41, extractor = int16, template = "${port}*1008${value}"),
    FieldDefinition(fieldName = "MuteDigitSelect", kind = miscKey, offset = 47, extractor = int8, template = "${port}*2090${value}"),
    FieldDefinition(fieldName = "CTCSSDuringID", kind = portKey, offset = 47, extractor = int8, template = "${port}*2090${value}"),
    FieldDefinition(fieldName = "TimeoutPorts", kind = miscKey, offset = 54, extractor = int8, template = "${port}*2051${value}"),


  )
}
