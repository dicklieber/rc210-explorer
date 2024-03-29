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

package net.wa9nnn.rc210.data.field

class FieldBooleanSpec extends RcSpec {

    "toCommand false" in {
      val fieldBoolean = FieldBoolean()
      val command = fieldBoolean.toCommands(new FieldEntryBase {
        override val fieldKey: FieldKey = FieldKey("nn", KeyFactory.defaultMacroKey)
        override val template: String = "1*999b"
      })
      command should have length 1
      command.head should equal ("1*9990")
    }
    "toCommand true" in {
      val fieldBoolean = FieldBoolean(true)
      val command = fieldBoolean.toCommands(new FieldEntryBase {
        override val fieldKey: FieldKey = FieldKey("nn", KeyFactory.defaultMacroKey)
        override val template: String = "1*999b"
      })
      command.head should equal ("1*9991")
    }
}
