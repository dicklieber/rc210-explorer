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
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.KeyKind.Common
import net.wa9nnn.rc210.data.clock.Clock.clockForm
import net.wa9nnn.rc210.data.clock.DSTPoint.dstPointForm
import net.wa9nnn.rc210.data.clock.{Clock, DSTPoint}
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.ui.ProcessResult
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*
import net.wa9nnn.rc210.security.Who.*
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.Who.given
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton()
class ClockController @Inject()(implicit dataStore: DataStore, ec: ExecutionContext,
                                components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {

  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] => {
      val clock: Clock = dataStore.editValue(Clock.fieldKey)
      Ok(views.html.clock(clockForm.fill(clock)))
    }
  }

  def save(): Action[AnyContent] = Action { implicit request:MessagesRequest[AnyContent] =>
    clockForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Clock]) => {
          BadRequest(views.html.clock(formWithErrors))
        },
        (clock: Clock) => {
          val candidateAndNames = ProcessResult(clock)

          given RcSession = request.attrs(sessionKey)

          dataStore.update(candidateAndNames)
          Redirect(routes.ClockController.index)
        }
      )
  }
}




