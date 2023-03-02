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

import net.wa9nnn.rc210.data.field.SelectOptions.macroSelect
import net.wa9nnn.rc210.data.field.UiInfo.{checkBox, unlockCode}
import net.wa9nnn.rc210.key.KeyKind._

object FieldDefinitions {

  import fieldDefintionSugar.FieldDefintionSugar._

  val fields: Seq[FieldMetadata] = Seq(
    (0, "Site Prefix", miscKey, "*2108v") % UiDtmf(3),
    (4, "TT PadTest", miscKey, "*2093v") % UiDtmf(5),
    (10, "Say Hours", miscKey, "*5104b") % checkBox,
    (11, "Hang Time 1", portKey, "n*10001v"),
    (14, "Hang Time 2", portKey, "n*10002v"),
    (17, "Hang Time 3", portKey, "n*10003v"),
    (20, "Initial ID Timer  ", portKey, "n*1000v") % UiNumber(255, "minutes"),
    (23, "Pending ID Timer  ", portKey, "n*1003v") % UiNumber(255, "minutes"),
    (26, "Tx Enable", portKey, "n11b") % checkBox,
    (29, "DTMF Cover Tone", portKey, "n13b") % checkBox,
    (32, "DTMF Mute Timer", portKey, "n*1006v") % UiNumber(999, "100 milliseconds"),
    (38, "Kerchunk", portKey, "n15b") % checkBox,
    (41, "Kerchunk Timer", portKey, "n1018b") % UiNumber(6000, "milliseconds"),
    (47, "Mute Digit Select", miscKey, "*2090v") % SelectOptions.dtmfMuteDigit,
    (48, "CTCSS During ID", portKey, "*n2089") % checkBox,
    (54, "Timeout Ports", miscKey, "*2051b") % checkBox,
    (55, "Speech Delay", miscKey, "*1019v") % UiNumber(600, "Seconds"),
    (57, "CTCSS Encode Polarity", portKey, "*1021v") % checkBox,
    (60, "Guest Macro Range", portKey, "*4009v") % UiTwoNumbers("<from macro> <to macro>"),
    (67, "DTMF COS Control", portKey, "n22b") % checkBox,
    (73, "DTMF Require Tone", portKey, "n17b") % checkBox,
    (76, "Unlock", portKey, "* 9 0 0 0 S") % unlockCode,
    (103, "Speech ID Override", portKey, "n20b") % checkBox,
    //todo  CwTone1 & CwTone2 span two blocks! Gag!  FieldDefinition(fieldName = "CwTone1", kind = portKey, offset = 106, extractor = cwTones, template = "${port}*8001'{bool}"),
    (118, "CWS Speed", portKey, "n*8000b") % checkBox,
    (136, "CTCSS Decode", portKey, "n112b") % checkBox,
    (139, "Monitor Mix", portKey, "n119b") % checkBox,
    //AuxAudioTimer - 142-147 //todo how does this match up with the macro to run?
    (148, "Inactivity Timeout", portKey, "n*1005") % UiNumber(255, "minutes"),
    (151, "Speech Override", portKey, "n*120v") % UiNumber(255, "minutes"),
    (154, "Encode Timer", portKey, "n*1007") % UiNumber(255, "minutes"),
    (157, "Repeat Mode", portKey, "n14b") % checkBox,
    (160, "Timeout Timer", portKey, "n*1001v") % UiNumber(32767, "seconds"),
    (166, "DTMF Mute", portKey, "n*1001v") % checkBox,
    (169, "Alarm Enable", portKey, "n191b") % checkBox,
    (174, "Alarm Macro Low", alarmKey, "*2101 n v") % macroSelect,

  )

}
