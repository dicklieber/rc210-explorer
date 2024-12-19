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

//import net.wa9nnn.rc210.KeyKind
//import net.wa9nnn.rc210.KeyKind.*
import net.wa9nnn.rc210.KeyKind.{Common, Port}
import net.wa9nnn.rc210.data.clock.ClockNode
import net.wa9nnn.rc210.data.courtesy.CourtesyToneNode
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.meter.{MeterNode, MeterAlarmNode}
import net.wa9nnn.rc210.data.remotebase.RemoteBaseNode
import net.wa9nnn.rc210.data.schedules.ScheduleNode
import net.wa9nnn.rc210.data.timers.TimerNode

import javax.inject.{Inject, Singleton}
@Singleton
class FieldDefinitions @Inject()() {
  /**
   * Everything about each rc2input.
   */
  val simpleFields: Seq[FieldDefinitionSimple] = Seq(
    FieldDefinitionSimple(0, "Site Prefix", Common, "1*2108v", FieldDtmf) max 3,
    FieldDefinitionSimple(4, "TT PadTest", Common, "1*2093v", FieldDtmf) max 5,
    FieldDefinitionSimple(10, "Say Hours", Common, "1*5104b", FieldBoolean),
    FieldDefinitionSimple(11, "Hang Time 1", Port, "n*10001v", FieldInt),
    FieldDefinitionSimple(14, "Hang Time 2", Port, "n*10002v", FieldInt),
    FieldDefinitionSimple(17, "Hang Time 3", Port, "n*10003v", FieldInt),
    FieldDefinitionSimple(20, "Initial ID Timer  ", Port, "n*1000v", FieldInt) units "minutes",
    FieldDefinitionSimple(23, "Pending ID Timer  ", Port, "n*1003v", FieldInt) units "minutes",
    FieldDefinitionSimple(26, "Tx Enable", Port, "nn10b", FieldBoolean),
    FieldDefinitionSimple(29, "DTMF Cover Tone", Port, "nn13b", FieldBoolean),
    FieldDefinitionSimple(32, "DTMF Mute Timer", Port, "n*1006v", FieldInt) max 999 units "100 ms",
    FieldDefinitionSimple(38, "Kerchunk", Port, "nn15b", FieldBoolean),
    FieldDefinitionSimple(41, "Kerchunk Timer", Port, "nn1018b", FieldInt) max 6000 units "ms",
//    SimpleField(47, "Mute Digit Select", Common, "n*2090v", MuteDigit), //todo
    FieldDefinitionSimple(48, "CTCSS During ID", Port, "n*2089b", FieldBoolean),
    FieldDefinitionSimple(54, "Timeout Ports", Common, "1*2051b", FieldBoolean),
    FieldDefinitionSimple(55, "Speech Delay", Common, "1*1019v", FieldInt) max 600 units "Seconds",
    FieldDefinitionSimple(57, "CTCSS Encode Polarity", Port, "n*1021v", FieldBoolean),
    FieldDefinitionSimple(60, "Guest Macro Range", Port, "n*4009v", Field2Numbers),
    FieldDefinitionSimple(67, "DTMF COS Control", Port, "nn22b", FieldBoolean),
    FieldDefinitionSimple(73, "DTMF Require Tone", Port, "nn17b", FieldBoolean),
    FieldDefinitionSimple(76, "Unlock", Port, "1*9000v", FieldDtmf) max 8,
    FieldDefinitionSimple(103, "Speech ID Override", Port, "nn20b", FieldBoolean),
    FieldDefinitionSimple(118, "CWS Speed", Port, "n*8000v", FieldInt) min 5 max 22 units "wpm",
    FieldDefinitionSimple(136, "CTCSS Decode", Port, "n112b", FieldBoolean),
    FieldDefinitionSimple(139, "Monitor Mix", Port, "n119b", FieldBoolean),
    //AuxAudioTimer - 142-147 //todo how does this match up with the macro to run?
    FieldDefinitionSimple(148, "Inactivity Timeout", Port, "n*1005v", FieldInt) max 255 units "minutes",
    FieldDefinitionSimple(151, "Speech Override", Port, "nn12b", FieldBoolean),
    FieldDefinitionSimple(154, "CTCSS Encode Timer", Port, "n*1007v", FieldInt) max 255 units "1/10 Seconds",
    FieldDefinitionSimple(157, "Repeat Mode", Port, "nn14b", FieldBoolean),
    FieldDefinitionSimple(160, "Timeout Timer", Port, "n*1001v", FieldInt) max 32767 units "seconds",
    FieldDefinitionSimple(166, "DTMF Mute", Port, "n121b", FieldBoolean),
    FieldDefinitionSimple(184, "vRef", Common, "1*2065v", FieldInt) max 20000,

    // needs to be complex rc2input   SimpleField(184, "Vref", meterKey, "*2065 n v", UiNumber(255, "todo three numbers?")), //*2065 4 9 6
    //###########################################################################################  //todo
    ///For meters, we gather all the parameters needed, then assemble them to actually store
    //###########################################################################################

    //MeterFaceName - 186-201
    //    *2064 C * M* X1* Y1* X2* Y2* C= Channel 1 to 8 M=Meter Type 0 to 6 X1, Y1, X2, Y2 represent two calibration points. There must be 6 parameters entered to define a meterEditor face, each value ending with *.

    FieldDefinitionSimple(322, "Rx Rcv Macro Active", Port, "n*2113 1 v", MacroKeyExtractor),
    FieldDefinitionSimple(325, "Rx Rcv Macro Low", Port, "n*2113  v", MacroKeyExtractor),
    //P1, P2, P3CWDI1 - 328-357
    //P1, P2, P3CWD2 - 358-402
    //P1, P2, P3INITIALID1 - 403-483
    //P1, P2, P3INITIALID2 - 484-549
    //P1, P2, P3INITIALID3 - 550-615

//    SimpleField(1176, "Radio Type", Common, "n*2083 v", RadioType),
//    SimpleField(1176, "Yaesu Type", Common, "n*2084 v", YaesuType),
    FieldDefinitionSimple(1177, "Fan Timeout", Common, "n*1004v", FieldInt) max 255 units "Minutes",
    //DTMFRegenPrefix1 - 1179-1185 need  special handling. part of IRLP stuff.
    FieldDefinitionSimple(1187, "Fan Select", Common, "n*2119b", FieldBoolean),
    FieldDefinitionSimple(1188, "DTMF Duration", Common, "n*2106v", FieldInt) max 256 units "Ms",
    FieldDefinitionSimple(1189, "DTMF Pause", Common, "n*2107b", FieldBoolean),
    //DTMFStrings - 1190-1409 special handling
    //DVRSecondLow - 1410-1473
    //DVRTrack - 1474-1493
    //DVRRowsUsed - 1494-1533
    FieldDefinitionSimple(1534, "Allow Terminator Speech", Common, "n*2091b", FieldBoolean),
    //RemoteRadioMode - 1535-1544
    FieldDefinitionSimple(1571, "Program Prefix", Common, "n*2109v", FieldDtmf) max 4 min 1,
    //Phrase - 1576-1975 Handled as MessageMacros.
    //IDExtras - 1976-1984 needs special handling
    //MacroPortLimit - 3425-3514 needs special handling probably should be a part of MacroNode
    FieldDefinitionSimple(3515, "Speak Pending ID Timer", Port, "n*1019v", FieldInt) max 600 units "seconds",
    FieldDefinitionSimple(3521, "Enable Speech ID", Port, "n*8008b", FieldBoolean),
    FieldDefinitionSimple(3524, "Guest Macro Enable", Port, "n280b", FieldBoolean),

    FieldDefinitionSimple(3531, "Lock Code", Common, "n*9010v", FieldDtmf) max 4,
    FieldDefinitionSimple(3536, "Terminator", Common, "1*9020v", FieldDtmf) max 1,
    //ClockCorrection - 3538-3539 Humm, only two bytes but doc shows:  Docs shows this as *5105! Not *5101! In any event needs some special handling.
    FieldDefinitionSimple(3540, "Say Year", Common, "n*5102b", FieldBoolean),
    FieldDefinitionSimple(3541, "Tail Message", Port, "n*21101v", MacroKeyExtractor),
    FieldDefinitionSimple(3544, "Tail Message", Port, "n*21102v", MacroKeyExtractor),
    FieldDefinitionSimple(3547, "Tail Message", Port, "n*21103v", MacroKeyExtractor),
    FieldDefinitionSimple(3550, "Tail Message Number", Port, "n*2111v", FieldInt),
    FieldDefinitionSimple(3553, "Tail Timer", Port, "n*1020v", FieldInt) max 999 units "tails" tooltip "0 disables",
    FieldDefinitionSimple(3559, "Tail Counter", Port, "n*2112v", FieldInt) max 999 units "tails" tooltip "0 disables",
    //FreqString - 3562-3641	remote base stuff
    //    FieldDefinition(fieldName = "FreqString", kind = portKey, offset = 3562, extractor = int8, template = "2112${value}")),
    //RemoteCTCSS - 3642-3651
    //CTCSSMode - 3652-3661
    //DTMFRegenPort1 - 3662 IRLP
    //DTMFRegenMacro1 - 3663 IRLP
    //APHangupCode - 3564-3669	 autopatch
    FieldDefinitionSimple(3670, "Use DR1", Port, "n*2124b", FieldBoolean)
    ,
    FieldDefinitionSimple(3671, "Timeout Reset Select", Port, "1*2122v", TimeoutTimerResetPoint)
    //todo finish
  )

  val m:ComplexFieldDefinition[MessageNode]  = MessageNode
  val complexFd: Seq[ComplexFieldDefinition[?]] = Seq(
    ScheduleNode,
    MacroNode,
    CourtesyToneNode,
    TimerNode,
    MessageNode,
    ClockNode,
    RemoteBaseNode,
    MeterNode,
    MeterAlarmNode,
    LogicAlarmNode
  )

  val allFields:Seq[FieldDefinition] = simpleFields ++ complexFd

  def forOffset(offset: Int): FieldDefinitionSimple = simpleFields.find(_.offset == offset).get

}
