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
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.field.{FieldValue, LogicAlarmNode}
import net.wa9nnn.rc210.data.meter.{MeterAlarmNode, MeterNode}
import net.wa9nnn.rc210.data.schedules.ScheduleNode
import net.wa9nnn.rc210.data.timers.TimerNode
import net.wa9nnn.rc210.ui.{Tab, Tabs}

sealed trait KeyKind(val maxN: Int, val handler: EditHandler[?] = null) extends EnumEntry
  with CapitalWords with Tab:
  override def indexUrl: String = routes.EditController.index(this).url

object KeyKind extends PlayEnum[KeyKind]:

  override def values: IndexedSeq[KeyKind] = findValues

  case object LogicAlarm extends KeyKind(5, LogicAlarmNode)

  case object Meter extends KeyKind(8, MeterNode)

  case object MeterAlarm extends KeyKind(8, MeterAlarmNode)

  case object DtmfMacro extends KeyKind(195)

  case object CourtesyTone extends KeyKind(10, net.wa9nnn.rc210.data.courtesy.CourtesyTone)

  case object Function extends KeyKind(1005)

  case object Macro extends KeyKind(105):
    override def indexUrl: String = routes.MacroController.index.url

  case object Message extends KeyKind(70): // 40 (in Main) + 30 (in RTC)
    override def indexUrl: String = routes.MessageController.index.url

  case object Clock extends KeyKind(1):
    override def indexUrl: String = routes.ClockController.index.url

  case object Port extends KeyKind(3):
    override def indexUrl: String = routes.PortsController.index.url

  case object Schedule extends KeyKind(40, ScheduleNode)


  case object Timer extends KeyKind(6, TimerNode)

  case object Common extends KeyKind(1):
    override def indexUrl: String = routes.CommonController.index.url

  case object RemoteBase extends KeyKind(1):
    override def indexUrl: String = routes.RemoteBaseController.index.url

