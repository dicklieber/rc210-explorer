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
import org.scalatest.BeforeAndAfterAll

import scala.language.postfixOps
import scala.runtime.stdLibPatches.Predef

class UserStoreTest extends WithTestConfiguration with BeforeAndAfterAll {
  private val wa9nnnDto: UserEditDTO = UserEditDTO("WA9NNN", Some("Dick"), Some("dick@u505.com"), Some("swordfish"), id = "1")
  private val user: User = User(wa9nnnDto)
  val rcSession: RcSession = RcSession("42", user, "")
  val noUser = new DefaultNoUsersLogin(config)
  val userStore: UserStore = new UserStore(config, noUser)
  //  Files.deleteIfExists(userStore.usersFile)
  userStore.put(wa9nnnDto)(rcSession)

  private val str: String = config.getString("vizRc210.dataDir")
  println(str)

  "UserStoreTest" when {
/*    "Initially exist because we did a put above" in {
      os.exists(userStore.usersFile) mustBe true
      val userRecords: UserRecords = userStore.readJson
      userRecords.users must have length 1
    }
*/    "editing" should {
      "Add new User" in {
        val lengthB4 = userStore.users.length

        val userDave = UserEditDTO(callsign = "WA9DEW",
          password = Some("swordfish"),
          id = "2")
        userStore.put(userDave)(rcSession)
        val users1 = userStore.users
        users1.length mustBe lengthB4 + 1
        userStore.remove(userDave.id)(rcSession)
        userStore.get(userDave.id) mustBe None

      }
      "update WA9NNN" in {
        val wa9nnnUser = userStore.users.find(_.callsign == "WA9NNN").get
        val dTO = wa9nnnUser.userEditDTO
        val updatedDto = dTO.copy(name = Some("Richard"))
        userStore.put(updatedDto)(rcSession)

        val maybeUser = userStore.get(dTO.id)
        maybeUser.get.userEditDTO mustBe updatedDto
      }
    }
    "validating a user" should {
      "good" in {
        val maybeUser = userStore.validate(Credentials("WA9NNN", "swordfish"))
        maybeUser.get.callsign mustBe "WA9NNN"
      }
      "fail with unknownCallsign" in {
        val maybeUser = userStore.validate(Credentials("W1AW", "swordfish"))
        maybeUser mustBe None
      }
      "fail with bad password" in {
        val maybeUser = userStore.validate(Credentials("WA9NNN", "crap"))
        maybeUser mustBe None
      }
    }

  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    val userRecords: UserRecords = userStore.readJson

    Predef.assert(userRecords.users.length == 1)
    Predef.assert(userRecords.who.callsign == "WA9NNN", s"who must be WA9NNN")
  }
}
