package net.wa9nnn.rc210

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

enum KeyIndicator(val char:Char):
  case Value extends KeyIndicator('|')
  case Name extends KeyIndicator('n')
  case Key extends KeyIndicator('k')


object KeyIndicator:
  def from(str: String): KeyIndicator =
    val ch = str.head
    values.find(_.char == ch).getOrElse(throw new IllegalArgumentException(s"Unknown indicator $ch"))

