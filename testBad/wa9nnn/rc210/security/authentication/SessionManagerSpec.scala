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

package net.wa9nnn.rc210.security.authentication

import controllers.UserEditDTO

import java.nio.file.Files

class SessionManagerSpec extends WithTestConfiguration {
  val user1: User = User(UserEditDTO(callsign = "w1aw", password = Option("swordfish")))
  val user2: User = User(UserEditDTO(callsign = "WA9DEW", password = Option("password")))
  "SessionManager" should {
    "Ensure only one session  per user" in {
      val path = Files.createTempFile("sessions", ".json")

      val sessionManager = new SessionManager(path)
      sessionManager.sessions.length should equal(0)
      val rcSession = sessionManager.create(user1, "127.00.1")
      sessionManager.sessions.length should equal(1)

      val rcSession2 = sessionManager.create(user1, "127.00.1")
      val rcSession3 = sessionManager.create(user1, "127.00.1")
      sessionManager.sessions.length should equal(1)
    }
  }
}
