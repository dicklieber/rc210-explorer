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
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{ComplexExtractor, FieldEntry}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.data.timers.{Timer, TimerExtractor}
import net.wa9nnn.rc210.ui.{ComplexFieldController, ProcessResult}
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Form
import play.api.mvc.*
import views.html.timerEditor
import net.wa9nnn.rc210.security.Who.request2Session
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class TimerController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends ComplexFieldController[Timer](dataStore, components) with LazyLogging {

  override val complexExtractor: ComplexExtractor = TimerExtractor

  override def indexResult(values: Seq[Timer]): Result = {
    Ok(views.html.timers(values))
  }

  override def editResult(filledForm: Form[Timer], namedKey: NamedKey)(using request: MessagesRequest[AnyContent]): Result =
    Ok(views.html.timerEditor(filledForm, namedKey))

  override def saveOkResult(): Result =
    Redirect(routes.TimerController.index())

  override val form: Form[Timer] =
    Timer.form
}