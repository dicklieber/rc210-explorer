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

package net.wa9nnn.rc210

import controllers.routes
import enumeratum.*
import enumeratum.EnumEntry.CapitalWords
import net.wa9nnn.rc210.data.{CommonNode, EditHandler}
import net.wa9nnn.rc210.data.clock.{ClockNode, Occurrence}
import net.wa9nnn.rc210.data.courtesy.CourtesyToneNode
import net.wa9nnn.rc210.data.field.{LogicAlarmNode, MessageNode}
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.meter.{MeterAlarmNode, MeterNode}
import net.wa9nnn.rc210.data.remotebase.RemoteBaseNode
import net.wa9nnn.rc210.data.schedules.ScheduleNode
import net.wa9nnn.rc210.data.timers.TimerNode
import net.wa9nnn.rc210.ui.{EnumEntryValue, Tab}

/**
 * @param maxN    how many keys there can be for this kind,
 * @param needsFieldName
 * @param includeInNav
 */
sealed trait KeyKind(val maxN: Int,
                     val handler: EditHandler = null,
                     val needsFieldName: Boolean = false,
                     val includeInNav: Boolean = true) extends EnumEntryValue
  with CapitalWords with Tab with Ordered[KeyKind]:
  override def indexUrl: String = routes.EditController.index(this).url

  override def values: IndexedSeq[EnumEntryValue] = KeyKind.values

  val rc210Value: Int = -1

  def compare(that: KeyKind): Int = this.entryName.compareTo(that.entryName)

object KeyKind extends PlayEnum[KeyKind]:

  override def values: IndexedSeq[KeyKind] = findValues

  case object All extends KeyKind(1, null, includeInNav = false)

  case object LogicAlarm extends KeyKind(5, LogicAlarmNode)

  case object Meter extends KeyKind(8, MeterNode)

  case object MeterAlarm extends KeyKind(8, MeterAlarmNode)

  case object CourtesyTone extends KeyKind(10, CourtesyToneNode)

  case object Function extends KeyKind(1005, needsFieldName = true, includeInNav = false)

  case object Macro extends KeyKind(105, MacroNode)

  case object Message extends KeyKind(70, MessageNode) // 40 (in Main) + 30 (in RTC)

  case object Clock extends KeyKind(1, ClockNode)

  case object Port extends KeyKind(3, PortsNode, needsFieldName = true)

  case object Schedule extends KeyKind(40, ScheduleNode)

  case object Timer extends KeyKind(6, TimerNode)

  case object Common extends KeyKind(1, CommonNode, needsFieldName = true)

  case object RemoteBase extends KeyKind(1, RemoteBaseNode)

