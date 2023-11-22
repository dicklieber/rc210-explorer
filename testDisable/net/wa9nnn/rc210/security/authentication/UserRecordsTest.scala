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
import net.wa9nnn.RcSpec
import net.wa9nnn.rc210.security.UserId.UserId

class UserRecordsTest extends RcSpec {

  "UserRecords" when {
    "empty" in {
      val userRecords = UserRecords()
      userRecords should have size 0
    }
    "add" in {
      val userRecords = UserRecords()
      val password = "12345"
      val updated = userRecords.update(UserEditDTO("WA9NNN", Option("Dick"), Option("rc210@u505.com"), password = Option(password)), who)
      userRecords should have size 0
      updated should have size 1
      val id: UserId = updated.users.head.id
      val backAgain: User = updated.get(id).get
      backAgain.id shouldBe id
      backAgain.callsign shouldBe("WA9NNN")

      val baUser: User = backAgain.validate(password).get
      baUser.callsign shouldBe "WA9NNN"

      val credentials = Credentials("WA9NNN", password)
      val maybeUser = updated.validate(credentials)
      val validatedUser: User = maybeUser.get
      validatedUser.id shouldBe id
    }


  }
}
