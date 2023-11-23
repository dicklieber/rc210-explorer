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

class ScheduleSpec extends WithMemory {
  val schedule: Schedule = Schedule(1)

  "Schedule" should {
    "toCommands" in {
      val fieldEntryBase: FieldEntryBase = new FieldEntryBase {
        override val fieldKey: FieldKey = schedule.fieldKey
        override val template: String = "schedule handle this internally."
      }
      val commands: Seq[String] = schedule toCommands fieldEntryBase
      commands.head should  equal("1*40011*0*00*00*00*01")
    }

/*
    "DOW single digit" in {
      pending
    }
    "DOW two digits becomes DOM" in {
      /*
       * However, you may alternately use 2 digits for DOW entry and it now becomes DOM (Day Of Month) and consists of 2 digits.
       * The first digit signifies which week within a month to use and the second digit signifies the day of that week to use.
       * For example, if an event is wanted for the 2nd Thursday of every month, you’d enter 24 for the DOW entry.
       */
      //      val dow = schedule.dow
      pending
    }
*/
  }
}
