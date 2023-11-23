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

package net.wa9nnn.rc210.serial

class MemorySpec extends WithMemory {
  "8bit ints" in {
    val value1: Iterator[Int] = memory.iterator8At(0)
    value1.next() should equal(65)
    value1.next() should equal(66)
    value1.next() should equal(67)
  }
  "16bit ints" in {
    val int16s: Iterator[Int] = memory.iterator16At(1553) // 1553 is timer seconds.
    val i0 = int16s.next()
    val i1 = int16s.next()
    int16s.next() should equal(4)
  }

  "chunks" in {

    val chunks: Seq[Chunk] = memory.chunks(76, 9, 1)
    chunks should have length(1)
    val head: Chunk = chunks.head
    head.size should equal(9)
    head.toString should equal ("72726")
  }
}
