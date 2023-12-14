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

package controllers

import net.wa9nnn.rc210.security.UserId
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who.Callsign
import play.api.data.Form
import play.api.data.Forms.*

/**
 *
 * @param callsign  user's callsign
 * @param name      friendly name
 * @param email     of user
 * @param id        a unique ID for the user. Not normally shown to users.
 * @param password  Some[String] with new password. None will keep existing password.
 */
case class UserEditDTO(callsign: Callsign = "", name: Option[Callsign] = None, email: Option[Callsign] = None, password: Option[Callsign] = None, id: UserId = UserId()) 

object UserEditDTO:
  def unapply(u: UserEditDTO): Option[(Callsign, Option[String], Option[String], UserId, Option[String])] =
    Some(u.callsign, u.name, u.email, u.id, u.password)

  val form: Form[UserEditDTO] = Form {
    mapping(
      "callsign" -> text,
      "name" -> optional(text),
      "email" -> optional(text),
      "id" -> text,
      "password" -> optional(text),
    )((callsign: Callsign, name: Option[Callsign], email: Option[Callsign], id: UserId, password: Option[Callsign]) => UserEditDTO.apply(callsign, name, email, password, id))(UserEditDTO.unapply)

  }
