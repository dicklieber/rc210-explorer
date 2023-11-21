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

package net.wa9nnn.rc210.data.field.schedule

import net.wa9nnn.rc210.util.select.{Rc210Item, SelectableNumber}

sealed trait Week(val rc210Value: Int, val display: String) extends Rc210Item

object Week extends SelectableNumber[Week] {

  case object Every extends Week(1, "Every")

  case object first extends Week(2, "first")

  case object second extends Week(3, "second")

  case object third extends Week(4, "third")

  case object forth extends Week(5, "forth")

  case object fifth extends Week(6, "fifth")
}

