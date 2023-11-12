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

package net.wa9nnn.rc210.util

import net.wa9nnn.rc210.key.{Key, KeyFactory}
import net.wa9nnn.rc210.security.authentication.User
import net.wa9nnn.rc210.security.authorzation.AuthFilter
import play.api.mvc.*
import play.api.mvc.Security.AuthenticatedRequest

import scala.language.implicitConversions

/**
 * @param request from browser.
 */
class FormHelper(implicit request: Request[AnyContent]) {

  private val encoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

  val user: User = AuthFilter.user


  def key[K <: Key](name: String): K = KeyFactory(apply(name))

  def apply(name: String): String = opt(name).getOrElse("")

  def opt(name: String): Option[String] = encoded(name).headOption
}

//object FormHelper:
//  given Conversion[String, Option[String]] with
//    def apply(name: String)(using formHelper:FormHelper): Option[String] = formHelper.option(name)
//
//  given Conversion[String, String] with
//    def apply(name: String)(using formHelper:FormHelper): String = formHelper.string(name)
//
//end FormHelper
