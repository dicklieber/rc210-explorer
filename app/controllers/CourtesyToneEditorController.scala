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
import net.wa9nnn.rc210.data.courtesy.{CourtesyTone, CourtesyTonesExtractor}
import net.wa9nnn.rc210.data.datastore
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*
import net.wa9nnn.rc210.security.Who.*

import javax.inject.*

class CourtesyToneEditorController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val cts: Seq[CourtesyTone] = dataStore.indexValues(KeyKind.courtesyToneKey)
      Ok(views.html.courtesyTones(cts))
  }

  def edit(key: Key): Action[AnyContent] = Action {
    implicit request =>
      key.check(KeyKind.courtesyToneKey)
      val fieldKey = CourtesyTonesExtractor.fieldKey(key)
      val courtesyTone: CourtesyTone = dataStore.editValue(fieldKey)

      implicit val f: Form[CourtesyTone] = CourtesyTone.form.fill(courtesyTone)
      Ok(views.html.courtesyToneEdit(key.namedKey))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      CourtesyTone.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[CourtesyTone]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey
            implicit val f: Form[CourtesyTone] = formWithErrors
            BadRequest(views.html.courtesyToneEdit(namedKey))
          },
          (courtesyTone: CourtesyTone) => {
            val candidateAndNames = ProcessResult(courtesyTone)
            dataStore.update(candidateAndNames)(session(request))
            Redirect(routes.CourtesyToneEditorController.index())
          }
        )
  }
}