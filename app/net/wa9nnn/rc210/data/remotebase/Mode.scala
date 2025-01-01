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

package net.wa9nnn.rc210.data.remotebase

import net.wa9nnn.rc210.data.field.Rc210EmumEntry
import net.wa9nnn.rc210.ui.Rc210Enum

sealed trait Mode(val rc210Value: Int) extends Rc210EmumEntry

object Mode extends Rc210Enum[Mode] {

  override val values: IndexedSeq[Mode] = findValues

  case object LSB extends Mode(1)

  case object USB extends Mode(2)

  case object CW extends Mode(3)
  case object FM extends Mode(4)
  case object AM extends Mode(5)
}
