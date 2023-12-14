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
import net.wa9nnn.rc210.WithTestConfiguration
import org.scalatest.Sequential

import scala.language.postfixOps

class UserStoreTest extends WithTestConfiguration {
  private val wa9nnnDto: UserEditDTO = UserEditDTO("WA9NNN", Some("Dick"), Some("dick@u505.com"), Some("swordfish"), id = "1")
  private val user: User = User(wa9nnnDto)
  val rcSession: RcSession = RcSession("42", user, "")
  val defaultNoUsersLogin = new DefaultNoUsersLogin(config)
  val userStore = new UserStore()(config, defaultNoUsersLogin)
  userStore.put(wa9nnnDto)(rcSession)

  "UserStoreTest" should {

    "Add new User" in {
      val lengthB4 = userStore.users.length

      val userDave = UserEditDTO(callsign = "WA9DEW",
        password = Some("swordfish"),
        id = "2")
      userStore.put(userDave)(rcSession)
      val users1 = userStore.users
      users1.length shouldBe lengthB4 + 1
      userStore.remove(userDave.id)(rcSession)
      userStore.get(userDave.id) shouldBe None

    }
    "update WA9NNN" in {
      val wa9nnnUser = userStore.users.find(_.callsign == "WA9NNN").get
      val dTO = wa9nnnUser.userEditDTO
      val updatedDto = dTO.copy(name = Some("Richard"))
      userStore.put(updatedDto)(rcSession)

      val maybeUser = userStore.get(dTO.id)
      maybeUser.get.userEditDTO shouldBe updatedDto
    }
    "validation" when {
      "good" in {
        val maybeUser = userStore.validate(Credentials("WA9NNN", "swordfish"))
        maybeUser.get.callsign should be("WA9NNN")
      }
      "unknownCallsign" in {
        val maybeUser = userStore.validate(Credentials("W1AW", "swordfish"))
        maybeUser shouldBe None
      }
      "bad password" in {
        val maybeUser = userStore.validate(Credentials("WA9NNN", "crap"))
        maybeUser shouldBe None
      }
    }
  }
}
