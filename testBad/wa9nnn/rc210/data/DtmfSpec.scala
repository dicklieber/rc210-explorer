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

package net.wa9nnn.rc210.data


class DtmfSpec extends RcSpec {
/*
//class DtmfSpec extends RcSpec with org.specs2.specification.Tables {
  override def is =
    s2"""

 adding integers should just work in scala ${
      // the header of the table, with `|` separated strings (`>` executes the table)
      "ints" | "expected" |>
        Seq(0, 0, 0, 0, 255) ! "" |
        Seq(0xa, 0, 0, 0, 255) ! "0" |
        Seq(0xb, 0, 0, 0, 255) ! "*" |
        Seq(0xc, 0, 0, 0, 255) ! "#" |
        Seq(33,3, 0, 0, 0) ! "123" |
        Seq(0x21, 3, 9, 0, 255) ! "123" |
        Seq(1, 0, 9, 0, 255) ! "1" |
        Seq(0x21, 0, 9, 0, 255) ! "12" |
        Seq(161, 169, 0xe1, 0, 255) ! "10901B" | // another example row
        { (ints, expected) =>
          val dtmf = Dtmf(ints)
          dtmf.get must beEqualTo(expected)
        } // the expectation to check on each row
    }
"""
*/
}
