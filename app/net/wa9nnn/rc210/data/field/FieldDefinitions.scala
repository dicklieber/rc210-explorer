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
    FieldDefinition(fieldName = "SitePrefix", kind = miscKey, offset = 0, extractor = dtmf, template = "*2108${value}"),
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
    FieldDefinition(fieldName = "MuteDigitSe˚lect", kind = miscKey, offset = 47, extractor = int8, template = "${port}*2090${value}"),
    FieldDefinition(fieldName = "CTCSSDuringID", kind = portKey, offset = 47, extractor = int8, template = "${port}*2090${value}"),
    FieldDefinition(fieldName = "TimeoutPorts", kind = miscKey, offset = 54, extractor = int8, template = "${port}*2051${value}"),
    FieldDefinition(fieldName = "SpeechDelay", kind = miscKey, offset = 55, extractor = int16, template = "${port}*1019${value}"),
    FieldDefinition(fieldName = "CTCSSEncodePolarity", kind = miscKey, offset = 57, extractor = bool, template = "${port}*1021${value}"),
    FieldDefinition(fieldName = "GuestMacroRange", kind = miscKey, offset = 60, extractor = cwTones, template = "${port}*4009${value}"),
    FieldDefinition(fieldName = "DTMFCOSControl", kind = portKey, offset = 67, extractor = bool, template = "${port}22${bool}"),
    FieldDefinition(fieldName = "DTMFRequireTone", kind = portKey, offset = 73, extractor = bool, template = "${port}17${bool}"),
    FieldDefinition(fieldName = "Unlock", kind = portKey, offset = 76, extractor = unlock, template = "${port}17${bool}"),
    FieldDefinition(fieldName = "SpeechIDOverride", kind = portKey, offset = 103, extractor = bool, template = "${port}18${bool}"),
    //todo  CwTone1 & CwTone2 span two blocks! Gag!  FieldDefinition(fieldName = "CwTone1", kind = portKey, offset = 106, extractor = cwTones, template = "${port}*8001'{bool}"),
    FieldDefinition(fieldName = "CWSpeed", kind = portKey, offset = 118, extractor = int8, template = "${port}*8000${value}"),
    FieldDefinition(fieldName = "CTCSSDecode", kind = portKey, offset = 136, extractor = bool, template = "${port}112${bool}"),
    FieldDefinition(fieldName = "MonitorMix", kind = portKey, offset = 139, extractor = bool, template = "${port}119${bool}"),
    //AuxAudioTimer - 142-147 //todo how does this match up with the macro to run?
    FieldDefinition(fieldName = "InActiveTimeout", kind = portKey, offset = 148, extractor = int8, template = "${port}*1005${value}"),
    FieldDefinition(fieldName = "Speechoverride", kind = portKey, offset = 151, extractor = bool, template = "${port}*120${bool}"),
    FieldDefinition(fieldName = "EncodeTimer", kind = portKey, offset = 151, extractor = int8, template = "${port}*1007${value}"),
    FieldDefinition(fieldName = "RepeatMode", kind = portKey, offset = 157, extractor = int8, template = "${port}14${bool}"),
    FieldDefinition(fieldName = "TimeoutTimer", kind = portKey, offset = 160, extractor = int16, template = "${port}*1001${bool}"), // 1 to 32767 seconds
    FieldDefinition(fieldName = "DTMFMute", kind = portKey, offset = 166, extractor = bool, template = "${port}21{bool}"),
    FieldDefinition(fieldName = "AlarmEnable", kind = portKey, offset = 169, extractor = int16, template = "${port}21{bool}"),
    FieldDefinition(fieldName = "AlarmMacroLow", kind = alarmKey, offset = 174, extractor = int16, template = "*2101 ${port} ${value}"),
    FieldDefinition(fieldName = "AlarmMacroHigh", kind = alarmKey, offset = 179, extractor = int16, template = "*2102 ${port} ${value}"),
    FieldDefinition(fieldName = "Vref", kind = alarmKey, offset = 184, extractor = int16, template = "*2065 ${port} ${value}"), //*2065 4 9 6

    //###########################################################################################  //todo
    ///For meters, we gather all the parameters needed, then assemble them to actually store
    //###########################################################################################

    //MeterFaceName - 186-201
    //    *2064 C * M* X1* Y1* X2* Y2* C= Channel 1 to 8 M=Meter Type 0 to 6 X1, Y1, X2, Y2 represent two calibration points. There must be 6 parameters entered to define a meter face, each value ending with *.

    FieldDefinition(fieldName = "RxRcvMacroActive", kind = alarmKey, offset = 322, extractor = int16, template = "*2113 1 ${value}"),
    FieldDefinition(fieldName = "RxRcvMacroLow", kind = alarmKey, offset = 325, extractor = int16, template = "*2113 0 ${value}"),
    //P1, P2, P3CWDI1 - 328-357
    //P1, P2, P3CWD2 - 358-402
    //P1, P2, P3INITIALID1 - 403-483
    //P1, P2, P3INITIALID2 - 484-549
    //P1, P2, P3INITIALID3 - 550-615

    //SetPointDOW - 616-655  handled in Schedule.

    // Courtesy Tone special handling.  *31CT Delay to segment 1 * duration of segment 1 * Tone 1 * Tone 2 *



    FieldDefinition(fieldName = "RadioType", kind = miscKey, offset = 1176, extractor = int8, template = "*2083  ${value}"), // need radio lookup for select control.
    FieldDefinition(fieldName = "YaesuType", kind = miscKey, offset = 1177, extractor = int8, template = "*2084  ${value}"), // need radio lookup for select control.
    FieldDefinition(fieldName = "FanTimeout", kind = miscKey, offset = 1177, extractor = int8, template = "*1004  ${value}"),
    //DTMFRegenPrefix1 - 1179-1185 need  special handling. part of IRLP stuff.
    FieldDefinition(fieldName = "Clock24Hours", kind = miscKey, offset = 1186, extractor = bool, template = "*5103${bool}"),
    FieldDefinition(fieldName = "FanSelect", kind = miscKey, offset = 1187, extractor = bool, template = "*2119${bool}"), // needs radio buttons
    FieldDefinition(fieldName = "DTMFDuration", kind = miscKey, offset = 1188, extractor = bool, template = "*2106${value}"), // milliseconds
    FieldDefinition(fieldName = "DTMFPause", kind = miscKey, offset = 1189, extractor = bool, template = "*2107${value}"), // milliseconds
    //DTMFStrings - 1190-1409 special handling
    //DVRSecondLow - 1410-1473
    //DVRTrack - 1474-1493
    //DVRRowsUsed - 1494-1533
    FieldDefinition(fieldName = "AllowTerminatorSpeech", kind = miscKey, offset = 1534, extractor = bool, template = "*2091${bool}"),
    //RemoteRadioMode - 1535-1544
    FieldDefinition(fieldName = "AutoPatchPort", kind = miscKey, offset = 1551, extractor = int8, template = "*2116${value}"),
    FieldDefinition(fieldName = "AutoPatchMute", kind = miscKey, offset = 1552, extractor = bool, template = "270${bool}"),
    //GeneralTimers1_3 - 1553-1558
    //GeneralTimers4_6 - 1559-1564
    //GeneralTimer1_3Macro - 1565-1567
    //GeneralTimer4_6Macro - 1568-1570
    FieldDefinition(fieldName = "ProgramPrefix", kind = miscKey, offset = 1571, extractor = dtmf, template = "*2109${value}"),
    //Phrase - 1576-1975 Handled as MessageMacros.
    //IDExtras - 1976-1984 needs special handling
    //MacroPortLimit - 3425-3514 needs special handling probably should be a part of MacroNode
    FieldDefinition(fieldName = "SpeakPendingIDTimer", kind = portKey, offset = 3515, extractor = int16, template = "*1019${value}"), // magic numbers 0 600
    FieldDefinition(fieldName = "EnableSpeechID", kind = portKey, offset = 3521, extractor = bool, template = "*8008${bool}"),
    FieldDefinition(fieldName = "GuestMacroEnable", kind = portKey, offset = 3524, extractor = bool, template = "280${bool}"),
    FieldDefinition(fieldName = "RemoteBasePrefix", kind = portKey, offset = 3525, extractor = dtmf, template = "*2060${value}"),
    FieldDefinition(fieldName = "LockCode", kind = portKey, offset = 3531, extractor = dtmf, template = "*9010${value}"), // 1-4 digits
    FieldDefinition(fieldName = "Terminator", kind = miscKey, offset = 3536, extractor = dtmf, template = "*9020${value}"), // 1 digit
    //ClockCorrection - 3538-3539 Humm, only two bytes but doc shows:  Docs shows this as *5105! Not *5101! In any event needs some special handling.
    FieldDefinition(fieldName = "SayYear", kind = miscKey, offset = 3540, extractor = bool, template = "*5102${bool}"), // 1 digit
    FieldDefinition(fieldName = "P1TailMessage", kind = portKey, offset = 3541, extractor = int8, template = "*2110 1 ${value}"),
    FieldDefinition(fieldName = "P2TailMessage", kind = portKey, offset = 3544, extractor = int8, template = "*2110 2 ${value}"),
    FieldDefinition(fieldName = "P3TailMessage", kind = portKey, offset = 3547, extractor = int8, template = "*2110 3 ${value}"),
    FieldDefinition(fieldName = "TailMessageNumber", kind = portKey, offset = 3550, extractor = int8, template = "*2111${value}"),
    FieldDefinition(fieldName = "TailTimer", kind = portKey, offset = 3553, extractor = int8, template = "*1020${value}"),
    FieldDefinition(fieldName = "TailCounter", kind = portKey, offset = 3559, extractor = int8, template = "2112${value}"),
    //FreqString - 3562-3641	remote base stuff
//    FieldDefinition(fieldName = "FreqString", kind = portKey, offset = 3562, extractor = int8, template = "2112${value}"),
    //RemoteCTCSS - 3642-3651
    //CTCSSMode - 3652-3661
    //DTMFRegenPort1 - 3662 IRLP
    //DTMFRegenMacro1 - 3663 IRLP
    //APHangupCode - 3564-3669	 autopatch
    FieldDefinition(fieldName = "UseDR1", kind = miscKey, offset = 3670, extractor = int8, template = "*2124${bool}"),
    FieldDefinition(fieldName = "TimeoutResetSelect", kind = portKey, offset = 3671, extractor = int8, template = "*2122${value}"), // select control
    FieldDefinition(fieldName = "KerchunkResetTimer", kind = portKey, offset = 3674, extractor = int8, template = "*1008${value}"),
    FieldDefinition(fieldName = "WindSelect", kind = miscKey, offset = 3677, extractor = bool, template = "*2123${bool}"),//Select Wind Direction Read back
    FieldDefinition(fieldName = "ConstantID", kind = portKey, offset = 3678, extractor = bool, template = "${port}*8009${bool}"),//Select Wind Direction Read back
    FieldDefinition(fieldName = "IDOnPTT", kind = portKey, offset = 3681, extractor = bool, template = "${port}*2121'${bool}"),//Select  = 1 PTT* x = 0 COS
    FieldDefinition(fieldName = "TOTResetTimer", kind = portKey, offset = 3684, extractor = int8, template = "${port}*1009'${bool}"), // 1/10 sec
    //DSTFlag - 3687  131x yy z where x = 1 is to program the START month and 0 is to program the END month yy is the month (01-12) and must be 2 digits and z Is the occurrence of Sunday in that month (1 – 5).
    //DTMFRegenPrefix2 IRLP©/Echolink©
    //DTMFRegenPrefix3 more IRLP
    //DTMFRegenPort2 - 3702
    //DTMFRegenPort3 - 3703
    //DTMFRegenMacro2 - 3704
    //DTMFRegenMacro3 - 3705
    //APOffHookCode - 3706-3711
    //APAutoDialCode - 3712-3717
    //APExtendTimerCode- 3718-3723
    //APExtendTimerCode- 3718-3723
    //P1, P2, P3PendingID1 - 3733-3776
    //P1, P2, P3PendingID2 - 3777-3864
    //P1, P2, P3PendingID3 - 3865-3930
    //ISDType - 3931 *7007 not in docs. SQL shows "Use Type 04 ISD DVR IC"  doesn't seem that it needs to be set.
    //DVRSecondHigh - 3932-3995
    //Total TX Uptime
    //Total TX Keyups - 4008 - 4019
    //TxKeySelect(4020)
    FieldDefinition(fieldName = "RSSILow", kind = miscKey, offset = 4021, extractor = int8, template = "*21301"), // not in docs
    FieldDefinition(fieldName = "RSSIHigh", kind = miscKey, offset = 4021, extractor = int8, template = "*21302"), // not in docs
    //ExtendedMacroPortLimit - 4027-4041 needs special handling probably should be a part of MacroNode
    //DST Start Date - 4042-4045
    //DST End Date - 4046-4049
    //DST Start Hour 1 or 2 AM - 4050
    FieldDefinition(fieldName = "VoiceResponseOn/Off", kind = miscKey, offset = 4052, extractor = bool, template = "**2135{bool}"),//Detailed Voice Responses Generic CW Response – ‘R’ for good, ‘?’ for error
    //DST End Hour 1 or 2 AM - 4053
    //Clock Delay - 4054 *****NOT USED AT THE MOMENT SO SUCK UP BYTE****
    //Extended Macros 1 - 390 (91 - 105)
  )
}
