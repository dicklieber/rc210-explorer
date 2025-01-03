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


import net.wa9nnn.rc210.KeyMetadata.{Common, Port}
import net.wa9nnn.rc210.PortsNode
import net.wa9nnn.rc210.data.CommonNode
import net.wa9nnn.rc210.data.clock.ClockNode
import net.wa9nnn.rc210.data.courtesy.CourtesyToneNode
import net.wa9nnn.rc210.data.field.DefInt
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.meter.{MeterAlarmNode, MeterNode}
import net.wa9nnn.rc210.data.remotebase.RemoteBaseNode
import net.wa9nnn.rc210.data.schedules.ScheduleNode
import net.wa9nnn.rc210.data.timers.TimerNode

import javax.inject.{Inject, Singleton}

@Singleton
class FieldDefinitions @Inject()() :
  /**
   * Everything about each rc2input.
   */
  val simpleFields: Seq[FieldDefSimple[?]] = Seq(
    DefDtmf(0, "Pre Access Code", Common, "1*2108v", maxDigits = 3),
    DefDtmf(4, "TT PadTest", Common, "1*2108v", maxDigits = 5),
    DefBool(10, "Hang Time 1", Port, "1*5104b"),
    DefInt(11, "Hang Time 1", Port, "n*10001v"),
    DefInt(14, "Hang Time 2", Port, "n*10002v"),
    DefInt(17, "Hang Time 3", Port, "n*10003v"),

  )


//  val simpleFields: Seq[FieldDefSimple] = Seq(
//    FieldDefSimple(0, "Site Prefix", Common, "1*2108v", FieldDtmf) max 3,
//    FieldDefSimple(4, "TT PadTest", Common, "1*2093v", FieldDtmf) max 5,
//    FieldDefSimple(10, "Say Hours", Common, "1*5104b", FieldBoolean),
//    FieldDefSimple(11, "Hang Time 1", Port, "n*10001v", FieldInt),
//    FieldDefSimple(14, "Hang Time 2", Port, "n*10002v", FieldInt),
//    FieldDefSimple(17, "Hang Time 3", Port, "n*10003v", FieldInt),
//    FieldDefSimple(20, "Initial ID Timer  ", Port, "n*1000v", FieldInt) units "minutes",
//    FieldDefSimple(23, "Pending ID Timer  ", Port, "n*1003v", FieldInt) units "minutes",
//    FieldDefSimple(26, "Tx Enable", Port, "nn10b", FieldBoolean),
//    FieldDefSimple(29, "DTMF Cover Tone", Port, "nn13b", FieldBoolean),
//    FieldDefSimple(32, "DTMF Mute Timer", Port, "n*1006v", FieldInt) max 999 units "100 ms",
//    FieldDefSimple(38, "Kerchunk", Port, "nn15b", FieldBoolean),
//    FieldDefSimple(41, "Kerchunk Timer", Port, "nn1018b", FieldInt) max 6000 units "ms",
////    SimpleField(47, "Mute Digit Select", Common, "n*2090v", MuteDigit), //todo
//    FieldDefSimple(48, "CTCSS During ID", Port, "n*2089b", FieldBoolean),
//    FieldDefSimple(54, "Timeout Ports", Common, "1*2051b", FieldBoolean),
//    FieldDefSimple(55, "Speech Delay", Common, "1*1019v", FieldInt) max 600 units "Seconds",
//    FieldDefSimple(57, "CTCSS Encode Polarity", Port, "n*1021v", FieldBoolean),
//    FieldDefSimple(60, "Guest Macro Range", Port, "n*4009v", Field2Numbers),
//    FieldDefSimple(67, "DTMF COS Control", Port, "nn22b", FieldBoolean),
//    FieldDefSimple(73, "DTMF Require Tone", Port, "nn17b", FieldBoolean),
//    FieldDefSimple(76, "Unlock", Port, "1*9000v", FieldDtmf) max 8,
//    FieldDefSimple(103, "Speech ID Override", Port, "nn20b", FieldBoolean),
//    FieldDefSimple(118, "CWS Speed", Port, "n*8000v", FieldInt) min 5 max 22 units "wpm",
//    FieldDefSimple(136, "CTCSS Decode", Port, "n112b", FieldBoolean),
//    FieldDefSimple(139, "Monitor Mix", Port, "n119b", FieldBoolean),
//    //AuxAudioTimer - 142-147 //todo how does this match up with the macro to run?
//    FieldDefSimple(148, "Inactivity Timeout", Port, "n*1005v", FieldInt) max 255 units "minutes",
//    FieldDefSimple(151, "Speech Override", Port, "nn12b", FieldBoolean),
//    FieldDefSimple(154, "CTCSS Encode Timer", Port, "n*1007v", FieldInt) max 255 units "1/10 Seconds",
//    FieldDefSimple(157, "Repeat Mode", Port, "nn14b", FieldBoolean),
//    FieldDefSimple(160, "Timeout Timer", Port, "n*1001v", FieldInt) max 32767 units "seconds",
//    FieldDefSimple(166, "DTMF Mute", Port, "n121b", FieldBoolean),
//    FieldDefSimple(184, "vRef", Common, "1*2065v", FieldInt) max 20000,
//
//    DefInt(11, "Hang Time 1", Port, "n*10001v"),
//    DefInt(14, "Hang Time 2", Port, "n*10002v"),
//    DefInt(17, "Hang Time 3", Port, "n*10003v"),
//
//
//
//  )

//  val m: FieldDefComplex[MessageNode] = MessageNode
  val complexDefs: Seq[FieldDefComplex[?]] = Seq(
    ScheduleNode,
    MacroNode,
    CourtesyToneNode,
    TimerNode,
    MessageNode,
    ClockNode,
    RemoteBaseNode,
    MeterNode,
    MeterAlarmNode,
    LogicAlarmNode,
//    PortsNode,
//    CommonNode,
  )
  val allFields: Seq[FieldDef[?]] = simpleFields ++ complexDefs
  
  private val fieldDefMap: Map[String, FieldDef[?]] = allFields.map(fd => fd.fieldName -> fd).toMap



  def lookup[T<:FieldDef[?]](name: String): T =
    fieldDefMap(name).asInstanceOf[T]

  



