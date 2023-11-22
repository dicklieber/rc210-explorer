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
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who.Callsign
import org.scalatest.matchers.must.Matchers._

import java.nio.file.{Files, Paths}

class UserManagerTest extends WithTestConfiguration {
  "UserManagerTest" when {
    val defaultLogin: Credentials = Credentials(new Callsign("DEFAULT"), "swordfish")
    val dir = Files.createTempDirectory("UserManagerTest")

    val path = dir.resolve("NoFile")
    "No users" should {
      val userManager = new UserManager(path, defaultLogin)
      "With non-default user" in {
        val maybeUser: Option[User] = userManager.validate(Credentials("WA9NNN", "123445"))
        maybeUser mustBe (None)
      }
      "With default user" in {
        val maybeUser: Option[User] = userManager.validate(defaultLogin)
        maybeUser.get.callsign must equal("DEFAULT")
      }
    }
    "Round trip" in {
      val userManager = new UserManager(path, defaultLogin)
      val password = Option("12345")
      val userEditDTO: UserEditDTO = UserEditDTO("WA9NNN", Option("Dick"), Option("wa9nnn@u505.com"), new UserId(), password, password)
      val who: User = User(userEditDTO)
      userManager.put(userEditDTO, who)

      userManager.users.users.length mustBe 1

    }

  }
}
