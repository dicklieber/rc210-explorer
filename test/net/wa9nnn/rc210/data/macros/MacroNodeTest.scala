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
import org.scalatest.matchers.must.Matchers.have

class MacroNodeTest extends WithMemory {

  "MacroNodeTest" should
    {
      "extract" when
        {
          val entries: Seq[FieldEntry] = MacroNode.extract(memory)
          "macro1" in
            {
              val fe: MacroNode = entries.head.fieldData.fieldValue.asInstanceOf[MacroNode]
              val string = fe.toString
              string mustBe "Macro1: dtmf: 10901 functions=165 85 27 60 196"
            }
          "macro2" in
            {
              val fe: MacroNode = entries(1).fieldData.fieldValue.asInstanceOf[MacroNode]
              val string = fe.toString
              string mustBe "Macro2: dtmf: 10902 functions=165 85 197 198"
            }
          "macro90" in
            {
              val fe: MacroNode = entries.last.fieldData.fieldValue.asInstanceOf[MacroNode]
              fe.dtmf.value mustBe("109090")
              fe.functions.length mustBe(0)
            }
          "length" in
            {
              entries must have length (90)
            }
        }
    }
}
