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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.data.clock.ClockNode.{fieldKey, form}
import net.wa9nnn.rc210.data.clock.{ClockNode, DSTPoint}
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.{ProcessResult, Tab}
import play.api.data.Form
import play.api.mvc.*
import views.html.NavMain

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class ClockController @Inject()(implicit dataStore: DataStore, ec: ExecutionContext,
                                components: MessagesControllerComponents, navMain: NavMain)
  extends MessagesAbstractController(components) with LazyLogging {

  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] => {
      val clock: ClockNode = dataStore.editValue(ClockNode.fieldKey)
      Ok(navMain(fieldKey.key.keyKind, views.html.clock(form.fill(clock))))
    }
  }

  def save(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ClockNode]) => {
          BadRequest(views.html.clock(formWithErrors))
        },
        (clock: ClockNode) => {
          val candidateAndNames = ProcessResult(clock)

          given RcSession = request.attrs(sessionKey)

          dataStore.update(candidateAndNames)
          Redirect(routes.ClockController.index)
        }
      )
  }
}




