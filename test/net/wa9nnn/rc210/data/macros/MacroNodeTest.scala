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

package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.{Key, RcSpec, WithMemory}

class MacroNodeTest extends WithMemory {

  "MacroNodeTest" should
    {
      "extract" in
        {
          val entries: Seq[FieldEntry] = MacroNode.extract(memory)
          val fe: MacroNode = entries.head.fieldData.fieldValue.asInstanceOf[MacroNode]
          val string = fe.toString
          string mustBe "Macro1: dtmf: 10901 functions=165 85 27 60 196"

          assert (entries.length > 40)
        }
    }
}
