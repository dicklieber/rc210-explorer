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

object FieldDefinitions {
  // template ${value}  ${bool} ${port}
  val fields: Seq[FieldDefinition] = Seq(
    FieldDefinition(fieldName = "SitePrefix", kind = "misc", offset = 0, bool, template = "*2108${value}"),
    FieldDefinition(fieldName = "TTPadTest", kind = "misc", offset = 4, extractor = dtmf, template = "*2093${value}"),
    FieldDefinition(fieldName = "SayHours", kind = "misc", offset = 10, bool, template = "*5104${bool}"),
    FieldDefinition(fieldName = "HangTime1", kind = "port", offset = 11, extractor = int8, template = "*10001${value}"),
    FieldDefinition(fieldName = "HangTime2", kind = "port", offset = 14, extractor = int8, template = "*10002${value}"),
    FieldDefinition(fieldName = "HangTime3", kind = "port", offset = 17, extractor = int8, template = "*10003${value}"),
    FieldDefinition(fieldName = "IIDMinutes", kind = "port", offset = 20, extractor = int8, template = "*1002${value}"),
    FieldDefinition(fieldName = "PIDMinutes", kind = "port", offset = 23, extractor = int8, template = "*1002${value}"),
    FieldDefinition(fieldName = "TxEnable", kind = "port", offset = 26, extractor = bool, template = "${port}111${bool}"),
    FieldDefinition(fieldName = "DTMFCoverTone", kind = "port", offset = 29, extractor = bool, template = "${port}113${bool}"),
    FieldDefinition(fieldName = "DTMFMuteTimer", kind = "port", offset = 32, extractor = int16, template = "${port}*1006${bool}"),
    FieldDefinition(fieldName = "Kerchunk", kind = "port", offset = 38, extractor = bool, template = "${port}*115${bool}"),
    FieldDefinition(fieldName = "KerchunkTimer", kind = "port", offset = 41, extractor = int16, template = "${port}*1008${value}"),
    FieldDefinition(fieldName = "MuteDigitSelect", kind = "misc", offset = 47, extractor = int8, template = "${port}*2090${value}"),
    FieldDefinition(fieldName = "CTCSSDuringID", kind = "port", offset = 47, extractor = int8, template = "${port}*2090${value}"),


  )
}
