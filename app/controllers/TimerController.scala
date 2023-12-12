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
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.timers.{Timer, TimerExtractor}
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.data.Form
import play.api.mvc.*
import views.html.timerEditor

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class TimerController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {

  def index: Action[AnyContent] = Action {
    implicit request =>
      val timers: Seq[Timer] = dataStore.indexValues(KeyKind.timerKey)
      Ok(views.html.timers(timers))
  }

  def edit(key: Key): Action[AnyContent] = Action {
    implicit request =>
      val timer: Timer = dataStore.editValue(TimerExtractor.fieldKey(key))
      Ok(views.html.timerEditor(Timer.form.fill(timer), key.namedKey))
  }

  def save(): Action[AnyContent] = Action {
    implicit request =>
      Timer.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[Timer]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey
            BadRequest(views.html.timerEditor(formWithErrors, namedKey))
          },
          (timer: Timer) => {
            val candidateAndNames = ProcessResult(timer)
            dataStore.update(candidateAndNames)
            Redirect(routes.TimerController.index)
          }
        )
  }
}
