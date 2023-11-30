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

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

case class UserData(name:String, age:Int)

object UserData:
  def unapply(u: UserData): Option[(String, Int)] = Some((u.name, u.age))
end UserData

// Form with Action extending MessagesAbstractController
class UserDataFormController @Inject() (components: MessagesControllerComponents)
  extends MessagesAbstractController(components) {

  val userForm: Form[UserData] = Form(
    mapping(
      "name" -> text,
      "age"  -> number
    )(UserData.apply)(UserData.unapply)
  )

  def index: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] => Ok(views.html.userData(userForm)) }

}