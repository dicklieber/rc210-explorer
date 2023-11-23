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

package net.wa9nnn.rc210.data.courtesy

//noinspection ZeroIndexToHead
class CourtesyExtractorSpec extends WithMemory {

  "CourtesyExtractorSpec" should {
    val cts: Seq[FieldEntry] = CourtesyExtractor.extract(memory)
    "extract" in {
      cts should have length (10)
    }
    "ct1" when  {
      val fieldEntry: FieldEntry = cts.head
      fieldEntry.fieldKey.toString should equal ("courtesyToneKey1:CourtesyTone")
      "commands" in {
        val fieldEntry: FieldEntry = cts.head
        val commands = fieldEntry.value.toCommands(fieldEntry)
        commands should have length (4)
        commands(0) should equal("1*31011200*100*660*880*")
        commands(1) should equal("1*320111*10*1600*1800*")
        commands(2) should equal("1*330122*20*2600*2800*")
        commands(3) should equal("1*340133*30*360*380*")
      }
    }
  }
}
