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

import net.wa9nnn.rc210.data.field.SelectOptions.{macroSelect, radioType}
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
    (174, "Alarm Macro Low", alarmKey, "*2101 n v") % UiSelect(macroSelect),
    (179, "Alarm Macro High", alarmKey, "*2102 n v") % UiSelect(macroSelect),
    (184, "Vref", alarmKey, "*2065 n v") % UiNumber(255, "todo three numbers?"),//*2065 4 9 6
    //###########################################################################################  //todo
    ///For meters, we gather all the parameters needed, then assemble them to actually store
    //###########################################################################################

    //MeterFaceName - 186-201
    //    *2064 C * M* X1* Y1* X2* Y2* C= Channel 1 to 8 M=Meter Type 0 to 6 X1, Y1, X2, Y2 represent two calibration points. There must be 6 parameters entered to define a meter face, each value ending with *.

    (322, "Rx Rcv Macro Active", alarmKey, "*2113 1 v") % UiNumber(255, "todo three numbers?"),//*2065 4 9 6
    (325, "Rx Rcv Macro Low", alarmKey, "*2113  v") % UiNumber(255, "todo three numbers?"),//*2065 4 9 6
    //P1, P2, P3CWDI1 - 328-357
    //P1, P2, P3CWD2 - 358-402
    //P1, P2, P3INITIALID1 - 403-483
    //P1, P2, P3INITIALID2 - 484-549
    //P1, P2, P3INITIALID3 - 550-615

    //SetPointDOW - 616-655  handled in Schedule.

    // Courtesy Tone special handling.  *31CT Delay to segment 1 * duration of segment 1 * Tone 1 * Tone 2 *

    (1176, "Radio Type", miscKey, "n*2083 v") % radioType,
    (1176, "Yaesu Type", miscKey, "n*2084 v") % radioType,
    (1177, "Fan Timeout", miscKey, "n*1004v") % radioType,
    //DTMFRegenPrefix1 - 1179-1185 need  special handling. part of IRLP stuff.
    (1186, "Clock 24 Hours", miscKey, "n*5103") % checkBox,
    (1187, "Fan Select", miscKey, "n*2119b") % checkBox,
    (1188, "DTMF Duration", miscKey, "n*2106b") % checkBox,
    (1189, "DTMF Pause", miscKey, "n*2107b") % checkBox,
    //DTMFStrings - 1190-1409 special handling
    //DVRSecondLow - 1410-1473
    //DVRTrack - 1474-1493
    //DVRRowsUsed - 1494-1533
    (1534, "Allow Terminator Speech", miscKey, "n*2091b") % checkBox,
    //RemoteRadioMode - 1535-1544
    (1534, "AutoPatch Port", miscKey, "n*2116v") % checkBox,
    (1534, "AutoPatch Port Mute", miscKey, "n270") % checkBox,
    //GeneralTimers1_3 - 1553-1558
    //GeneralTimers4_6 - 1559-1564
    //GeneralTimer1_3Macro - 1565-1567
    //GeneralTimer4_6Macro - 1568-1570
    (1571, "Program Prefix", miscKey, "n*2109") % UiDtmf(4),
    //Phrase - 1576-1975 Handled as MessageMacros.
    //IDExtras - 1976-1984 needs special handling
    //MacroPortLimit - 3425-3514 needs special handling probably should be a part of MacroNode
    (3515, "Speak Pending ID Timer", portKey, "n*1019v") % UiNumber(600, "seconds"),
    (3521, "Enable Speech ID", portKey, "n*8008b") % checkBox,
    (3524, "Guest Macro Enable", portKey, "n280b") % checkBox,
    (3525, "Remote Base Prefix", portKey, "n*2060v") % UiDtmf(5),
    (3531, "Lock Code", miscKey, "n*9010v") % UiDtmf(4),
    (3536, "Terminator", miscKey, "n9020v") % UiDtmf(1),
    //ClockCorrection - 3538-3539 Humm, only two bytes but doc shows:  Docs shows this as *5105! Not *5101! In any event needs some special handling.
    (3540, "Say Year", miscKey, "n*5102b") % checkBox,
    (3541, "P1 Tail Message", portKey, "n*2110 1 v") % UiSelect(macroSelect),
    (3544, "P2 Tail Message", portKey, "n*2110 2 v") % UiSelect(macroSelect),
    (3547, "P3 Tail Message", portKey, "n*2110 3 v") % UiSelect(macroSelect),
    (3550, "TailMessageNumber", portKey, "n*2111v") % UiSelect(macroSelect),
    (3553, "Tail Timer", portKey, "n*1020v") % UiNumber(999, "tails 0 disables"),
    (3559, "Tail Counter", portKey, "n*2112v") % UiNumber(999, "tails 0 disables"),
    //FreqString - 3562-3641	remote base stuff
    //    FieldDefinition(fieldName = "FreqString", kind = portKey, offset = 3562, extractor = int8, template = "2112${value}"),
    //RemoteCTCSS - 3642-3651
    //CTCSSMode - 3652-3661
    //DTMFRegenPort1 - 3662 IRLP
    //DTMFRegenMacro1 - 3663 IRLP
    //APHangupCode - 3564-3669	 autopatch
    (3670, "Use DR1", portKey, "n*2124b") % checkBox,
    (3671, "Timeout Reset Select", portKey, "nv") % checkBox,

    //todo finish
  )

}
