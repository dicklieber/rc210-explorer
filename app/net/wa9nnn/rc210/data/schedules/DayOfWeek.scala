/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */

package net.wa9nnn.rc210.data.schedules

import enumeratum.*

import net.wa9nnn.rc210.ui.{EnumEntryValue, EnumValue}


sealed trait DayOfWeek(val rc210Value: Int) extends  EnumEntryValue:
  override def values: IndexedSeq[EnumEntryValue] = DayOfWeek.values

object DayOfWeek extends EnumValue[DayOfWeek] {

  override val values: IndexedSeq[DayOfWeek] = findValues

  case object EveryDay extends DayOfWeek(0)

  case object Monday extends DayOfWeek(1)

  case object Tuesday extends DayOfWeek(2)

  case object Wednesday extends DayOfWeek(3)

  case object Thursday extends DayOfWeek(4)

  case object Friday extends DayOfWeek(5)

  case object Saturday extends DayOfWeek(6)

  case object Sunday extends DayOfWeek(7)

  case object Weekdays extends DayOfWeek(8)

  case object Weekends extends DayOfWeek(9)
}
