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

import net.wa9nnn.rc210.ui.{EnumEntryValue, EnumValue}


sealed trait WeekInMonth(val rc210Value: Int, val display: String) extends EnumEntryValue:
  override def values: IndexedSeq[EnumEntryValue] = WeekInMonth.values

object WeekInMonth extends EnumValue[WeekInMonth] {

  override val values: IndexedSeq[WeekInMonth] = findValues

  case object Every extends WeekInMonth(1, "Every")

  case object first extends WeekInMonth(2, "first")

  case object second extends WeekInMonth(3, "second")

  case object third extends WeekInMonth(4, "third")

  case object forth extends WeekInMonth(5, "forth")

  case object fifth extends WeekInMonth(6, "fifth")
}

