/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful),
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.MemoryExtractor
import net.wa9nnn.rc210.data.courtesy.CourtesyExtractor
import net.wa9nnn.rc210.data.field.UiInfo._
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.data.timers.TimerExtractor
import net.wa9nnn.rc210.key.KeyKind._

import javax.inject.{Inject, Singleton}

@Singleton
class FieldDefinitions @Inject()() {
  /**
   * Everything about each field.
   */
  val simplefields: Seq[SimpleField] = Seq(
    //offset  Name        KindOfKey     commandTemplate  how to render and parse
    SimpleField(0, "Site Prefix", commonKey, "*2108v", UiDtmf(3)),
    SimpleField(4, "TT PadTest", commonKey, "*2093v", UiDtmf(5)),
    SimpleField(10, "Say Hours", commonKey, "*5104b", checkBox),
    SimpleField(11, "Hang Time 1", portKey, "n*10001v", default),
    SimpleField(14, "Hang Time 2", portKey, "n*10002v"),
    SimpleField(17, "Hang Time 3", portKey, "n*10003v"),
    SimpleField(20, "Initial ID Timer  ", portKey, "n*1000v", UiNumber(255, "minutes")),
    SimpleField(23, "Pending ID Timer  ", portKey, "n*1003v", UiNumber(255, "minutes")),
    SimpleField(26, "Tx Enable", portKey, "n11b", checkBox),
    SimpleField(29, "DTMF Cover Tone", portKey, "n13b", checkBox),
    SimpleField(32, "DTMF Mute Timer", portKey, "n*1006v", UiNumber(999, "100 ms")),
    SimpleField(38, "Kerchunk", portKey, "n15b", checkBox),
    SimpleField(41, "Kerchunk Timer", portKey, "n1018b", UiNumber(6000, "ms")),
    SimpleField(47, "Mute Digit Select", commonKey, "*2090v", UISelect(
      "1st digit" -> 1,
      "2nd digit" -> 2)),
    SimpleField(48, "CTCSS During ID", portKey, "*n2089", checkBox),
    SimpleField(54, "Timeout Ports", commonKey, "*2051b", checkBox),
    SimpleField(55, "Speech Delay", commonKey, "*1019v", UiNumber(600, "Seconds")),
    SimpleField(57, "CTCSS Encode Polarity", portKey, "*1021v", checkBox),
    SimpleField(60, "Guest Macro Range", portKey, "*4009v", twoNumbers),
    SimpleField(67, "DTMF COS Control", portKey, "n22b", checkBox),
    SimpleField(73, "DTMF Require Tone", portKey, "n17b", checkBox),
    SimpleField(76, "Unlock", portKey, "* 9 0 0 0 S", unlockCode),
    SimpleField(103, "Speech ID Override", portKey, "n20b", checkBox),
    //todo  CwTone1 & CwTone2 span two blocks! Gag!  FieldDefinition(fieldName = "CwTone1", kind = portKey, offset = 106, extractor = cwTones, template = "${port}*8001'{bool}")),
    SimpleField(118, "CWS Speed", portKey, "n*8000b", UiRange(5, 22, "wpm")),
    SimpleField(136, "CTCSS Decode", portKey, "n112b", checkBox),
    SimpleField(139, "Monitor Mix", portKey, "n119b", checkBox),
    //AuxAudioTimer - 142-147 //todo how does this match up with the macro to run?
    SimpleField(148, "Inactivity Timeout", portKey, "n*1005", UiNumber(255, "minutes")),
    SimpleField(151, "Speech Override", portKey, "n*120v", UiNumber(255, "minutes")),
    SimpleField(154, "Encode Timer", portKey, "n*1007", UiNumber(255, "minutes")),
    SimpleField(157, "Repeat Mode", portKey, "n14b", checkBox),
    SimpleField(160, "Timeout Timer", portKey, "n*1001v", UiNumber(32767, "seconds")),
    SimpleField(166, "DTMF Mute", portKey, "n*1001v", checkBox),
    SimpleField(169, "Enable", logicAlarmKey, "n191b", checkBox),
    SimpleField(174, "Macro Low", logicAlarmKey, "*2101 n v", macroSelect),
    SimpleField(179, "Macro High", logicAlarmKey, "*2102 n v", macroSelect),
    SimpleField(184, "Vref", meterKey, "*2065 n v", UiNumber(255, "todo three numbers?")), //*2065 4 9 6
    //###########################################################################################  //todo
    ///For meters, we gather all the parameters needed, then assemble them to actually store
    //###########################################################################################

    //MeterFaceName - 186-201
    //    *2064 C * M* X1* Y1* X2* Y2* C= Channel 1 to 8 M=Meter Type 0 to 6 X1, Y1, X2, Y2 represent two calibration points. There must be 6 parameters entered to define a meter face, each value ending with *.

    SimpleField(322, "Rx Rcv Macro Active", portKey, "*2113 1 v", UiNumber(255, "todo three numbers?")), //*2065 4 9 6
    SimpleField(325, "Rx Rcv Macro Low", portKey, "*2113  v", UiNumber(255, "todo three numbers?")), //*2065 4 9 6
    //P1, P2, P3CWDI1 - 328-357
    //P1, P2, P3CWD2 - 358-402
    //P1, P2, P3INITIALID1 - 403-483
    //P1, P2, P3INITIALID2 - 484-549
    //P1, P2, P3INITIALID3 - 550-615

    //SetPointDOW - 616-655  handled in Schedule.

    // Courtesy Tone special handling.  *31CT Delay to segment 1 * duration of segment 1 * Tone 1 * Tone 2 *

    SimpleField(1176, "Radio Type", commonKey, "n*2083 v", UISelect(
      "Kenwood" -> 1,
      "Icom" -> 2,
      "Yaesu" -> 3,
      "Kenwood V7a" -> 4,
      "Doug Hall RBI - 1" -> 5,
      // there is no 6
      "Kenwood g707" -> 7,
      "Kenwood 271A" -> 8,
      "Kenwood V71a" -> 9)),


    SimpleField(1176, "Yaesu Type", commonKey, "n*2084 v", UISelect(
      "FT-100D" -> 1,
      "FT817, FT-857, FT-897" -> 2,
      "FT847" -> 3))
    ,
    SimpleField(1177, "Fan Timeout", commonKey, "n*1004v", UiNumber(255, "Minutes"))
    ,
    //DTMFRegenPrefix1 - 1179-1185 need  special handling. part of IRLP stuff.
    SimpleField(1186, "Clock 24 Hours", commonKey, "n*5103", checkBox)
    ,
    SimpleField(1187, "Fan Select", commonKey, "n*2119b", checkBox)
    ,
    SimpleField(1188, "DTMF Duration", commonKey, "n*2106b", checkBox)
    ,
    SimpleField(1189, "DTMF Pause", commonKey, "n*2107b", checkBox)
    ,
    //DTMFStrings - 1190-1409 special handling
    //DVRSecondLow - 1410-1473
    //DVRTrack - 1474-1493
    //DVRRowsUsed - 1494-1533
    SimpleField(1534, "Allow Terminator Speech", commonKey, "n*2091b", checkBox)
    ,
    //RemoteRadioMode - 1535-1544
    SimpleField(1534, "AutoPatch Port", commonKey, "n*2116v", checkBox)
    ,
    SimpleField(1534, "AutoPatch Port Mute", commonKey, "n270", checkBox)
    ,
    //GeneralTimers1_3 - 1553-1558
    //GeneralTimers4_6 - 1559-1564
    //GeneralTimer1_3Macro - 1565-1567
    //GeneralTimer4_6Macro - 1568-1570
    SimpleField(1571, "Program Prefix", commonKey, "n*2109", UiDtmf(4))
    ,
    //Phrase - 1576-1975 Handled as MessageMacros.
    //IDExtras - 1976-1984 needs special handling
    //MacroPortLimit - 3425-3514 needs special handling probably should be a part of MacroNode
    SimpleField(3515, "Speak Pending ID Timer", portKey, "n*1019v", UiNumber(600, "seconds"))
    ,
    SimpleField(3521, "Enable Speech ID", portKey, "n*8008b", checkBox)
    ,
    SimpleField(3524, "Guest Macro Enable", portKey, "n280b", checkBox)
    ,
    SimpleField(3525, "Remote Base Prefix", portKey, "n*2060v", UiDtmf(5))
    ,
    SimpleField(3531, "Lock Code", commonKey, "n*9010v", UiDtmf(4))
    ,
    SimpleField(3536, "Terminator", commonKey, "n9020v", UiDtmf(1))
    ,
    //ClockCorrection - 3538-3539 Humm, only two bytes but doc shows:  Docs shows this as *5105! Not *5101! In any event needs some special handling.
    SimpleField(3540, "Say Year", commonKey, "n*5102b", checkBox)
    ,
    SimpleField(3541, "P1 Tail Message", portKey, "n*2110 1 v", UiInfo.macroSelect)
    ,
    SimpleField(3544, "P2 Tail Message", portKey, "n*2110 2 v", UiInfo.macroSelect)
    ,
    SimpleField(3547, "P3 Tail Message", portKey, "n*2110 3 v", UiInfo.macroSelect)
    ,
    SimpleField(3550, "Tail Message Number", portKey, "n*2111v", UiInfo.macroSelect)
    ,
    SimpleField(3553, "Tail Timer", portKey, "n*1020v", UiNumber(999, "tails"))
    ,
    SimpleField(3559, "Tail Counter", portKey, "n*2112v", UiNumber(999, "tails", "0 disables"))
    ,
    //FreqString - 3562-3641	remote base stuff
    //    FieldDefinition(fieldName = "FreqString", kind = portKey, offset = 3562, extractor = int8, template = "2112${value}")),
    //RemoteCTCSS - 3642-3651
    //CTCSSMode - 3652-3661
    //DTMFRegenPort1 - 3662 IRLP
    //DTMFRegenMacro1 - 3663 IRLP
    //APHangupCode - 3564-3669	 autopatch
    SimpleField(3670, "Use DR1", portKey, "n*2124b", checkBox)
    ,
    SimpleField(3671, "Timeout Reset Select", portKey, "nv", checkBox)
    //todo finish
  )

  val complexFd: Seq[MemoryExtractor] = Seq(
    Schedule,
    MacroNode,
    CourtesyExtractor,
    TimerExtractor
  )

  def forOffset(offset: Int): SimpleField = simplefields.find(_.offset == offset).get

}
