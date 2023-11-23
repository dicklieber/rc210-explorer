package net.wa9nnn.rc210

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

import net.wa9nnn.rc210.security.Who
import org.scalatest.*
import org.scalatest.matchers.*
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}

abstract class RcSpec extends AnyWordSpec with should.Matchers with OptionValues with Inside with Inspectors {
  val who: Who = Who("testCs")

}

//abstract class RAsyncSpec extends AsyncWordSpec with should.Matchers with
//  OptionValues with Inside with Inspectors