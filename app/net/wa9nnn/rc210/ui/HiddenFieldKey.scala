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

package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.KeyIndicator.iKey
import play.twirl.api.Html

import scala.xml.*

/**
 *  Adds a hidden <<input>> with the [[Key]]
 */
object HiddenFieldKey:
  def apply(key: Key): Html =
    val parmName = key.withIndicator(iKey).id
    val r: Elem =
      <input type="hidden" name={parmName} value={key.name}></input>
    Html(r.toString)