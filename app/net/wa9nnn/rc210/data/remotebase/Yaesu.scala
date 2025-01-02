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

import net.wa9nnn.rc210.data.field.Rc210EnumEntry
import net.wa9nnn.rc210.ui.Rc210Enum

sealed trait Yaesu(val rc210Value: Int) extends Rc210EnumEntry

object Yaesu extends Rc210Enum[Yaesu]:

  override val values: IndexedSeq[Yaesu] = findValues

  case object FT100D extends Yaesu(1)

  case object FT817_857_897 extends Yaesu(2)


  case object FT847 extends Yaesu(3)


