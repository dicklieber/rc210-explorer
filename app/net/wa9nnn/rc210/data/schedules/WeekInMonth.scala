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

import net.wa9nnn.rc210.KeyMetadata.All.rc210Value
import net.wa9nnn.rc210.data.field.Rc210EnumEntry
import net.wa9nnn.rc210.data.schedules.WeekInMonth.Every
import net.wa9nnn.rc210.ui.Rc210Enum


sealed trait WeekInMonth(val rc210Value: Int) extends Rc210EnumEntry

object WeekInMonth extends Rc210Enum[WeekInMonth] :

  override val values: IndexedSeq[WeekInMonth] = findValues

  case object Every extends WeekInMonth(0)

  case object First extends WeekInMonth(1)

  case object Second extends WeekInMonth(2)

  case object Third extends WeekInMonth(3)

  case object Forth extends WeekInMonth(4)

  case object Fifth extends WeekInMonth(5)
  
  /**
   * Combine [[WeekInMonth]] and [[DayOfWeek]] into the one or two digit Dow field
   * that goes into the RC-210 command.
   *
   * @param dayOfWeek
   * @return one or two digits.
   */
  def translate(weekInMonth: WeekInMonth, dayOfWeek: DayOfWeek): String =
    if weekInMonth == Every then
      dayOfWeek.rc210Value.toString
    else
      s"${weekInMonth.rc210Value}${dayOfWeek.rc210Value}"


