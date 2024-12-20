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
 * Metadata about a kind of [[Key]].
 *
 * @param maxN    how many keys there can be for this kind,
 *                if 1 then the [[Key.rc210Number]] with not be used.
 * @param handler how to edit one of these.
 * @param needsQualifier
 * @param includeInNavTab
 */
sealed trait KeyMetadata(val maxN: Int,
                         val handler: EditHandler = null,
                         val needsQualifier: Boolean = false,
                         val includeInNavTab: Boolean = true,
                         val needsFieldName:Boolean = false,
                    ) extends EnumEntryValue with EditHandler
  with CapitalWords with Tab with Ordered[KeyMetadata]:
  override def indexUrl: String = routes.EditController.index(this).url

  override def values: IndexedSeq[EnumEntryValue] = KeyMetadata.values

  val rc210Value: Int = -1

  def compare(that: KeyMetadata): Int = this.entryName.compareTo(that.entryName)

object KeyMetadata extends PlayEnum[KeyMetadata]:

  override def values: IndexedSeq[KeyMetadata] = findValues

  case object All extends KeyMetadata(1, null, includeInNavTab = false)

  case object LogicAlarm extends KeyMetadata(5, LogicAlarmNode)

  case object Meter extends KeyMetadata(8, MeterNode)

  case object MeterAlarm extends KeyMetadata(8, MeterAlarmNode)

  case object CourtesyTone extends KeyMetadata(10, CourtesyToneNode)

  case object Function extends KeyMetadata(1005, needsFieldName = true, includeInNavTab = false)

  case object Macro extends KeyMetadata(105, MacroNode)

  case object Message extends KeyMetadata(70, MessageNode) // 40 (in Main) + 30 (in RTC)

  case object Clock extends KeyMetadata(1, ClockNode)

  case object Port extends KeyMetadata(3, PortsNode, needsFieldName = true)

  case object Schedule extends KeyMetadata(40, ScheduleNode)

  case object Timer extends KeyMetadata(6, TimerNode)

  case object Common extends KeyMetadata(1, CommonNode, needsFieldName = true)

  case object RemoteBase extends KeyMetadata(1, RemoteBaseNode)

