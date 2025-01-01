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
    //TTPadTest - 4-9
    DefBool(10, "Say Hours", Common, "1*5104b"),

    DefInt(11, "Hang Time 1", Port, "n*10001v"),
    DefInt(14, "Hang Time 2", Port, "n*10002v"),
    DefInt(17, "Hang Time 3", Port, "n*10003v"),



  )

//  val m: FieldDefComplex[MessageNode] = MessageNode
  val defs: Seq[FieldDef[?]] = Seq(
    ScheduleNode,
/*
    MacroNode,
    CourtesyToneNode,
    TimerNode,
    MessageNode,
    ClockNode,
    RemoteBaseNode,
    MeterNode,
    MeterAlarmNode,
    LogicAlarmNode,
    PortsNode,
    CommonNode,
*/
  )
  val allFields: Seq[FieldDef[?]] = simpleFields ++ defs
  
  private val fieldDefMap: Map[String, FieldDef[?]] = allFields.map(fd => fd.fieldName -> fd).toMap



  def lookup[T<:FieldDef[?]](name: String): T =
    fieldDefMap(name).asInstanceOf[T]

  



