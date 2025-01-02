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

import net.wa9nnn.rc210.data.field.TotReset.values
import net.wa9nnn.rc210.ui.Rc210Enum


sealed trait TotReset(val rc210Value: Int) extends Rc210EnumEntry:
  override val vals: Seq[Rc210EnumEntry] = values

object TotReset extends Rc210Enum[TotReset] :

  override val values: IndexedSeq[TotReset] = findValues

  case object AfterCOS extends TotReset(0)
  case object AfterCTSegment1 extends TotReset(1)
  case object AfterCTSegment2 extends TotReset(2)
  case object AfterCTSegment3 extends TotReset(3)
  case object AfterCTSegment4 extends TotReset(4)

