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

package net.wa9nnn.rc210.data.schedules

import net.wa9nnn.rc210.util.SelectField

object ScheduleEnums {
  val dayOfWeek: Seq[(String, Int)] = Seq(
    "EveryDay" -> 0,
    "Monday" -> 1,
    "Tuesday" -> 2,
    "Wednesday" -> 3,
    "Thursday" -> 4,
    "Friday" -> 5,
    "Saturday" -> 6,
    "Sunday" -> 7,
    "Weekdays" -> 8,
    "Weekends" -> 9
  )

  val monthOfYear: Seq[(String, Int)] = Seq(
    "Every Month" -> 0,
    "January" -> 1,
    "February" -> 2,
    "March" -> 3,
    "April" -> 4,
    "May" -> 5,
    "June" -> 6,
    "July" -> 7,
    "August" -> 8,
    "September" -> 9,
    "October" -> 10,
    "November" -> 11,
    "December" -> 12,

  )
}
