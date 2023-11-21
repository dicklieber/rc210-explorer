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

import net.wa9nnn.rc210.util.select.{SelectBase, Rc210Item}

sealed trait Yaesu(val rc210Value: Int, val display: String) extends Rc210Item

object Yaesu extends SelectBase[Yaesu]:
  case object FT100D extends Yaesu(1, "FT-100D")

  case object FT817_857_897 extends Yaesu(2, "FT817, FT-857, FT-897")

  case object FT847 extends Yaesu(3, "FT847")

