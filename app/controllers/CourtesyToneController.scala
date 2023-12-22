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
import net.wa9nnn.rc210.data.field.{ComplexExtractor, FieldEntry}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.{ComplexFieldController, ProcessResult}
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.data.Form
import play.api.mvc.*

import javax.inject.*

class CourtesyToneController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends ComplexFieldController[CourtesyTone](dataStore, components) with LazyLogging {

  override val complexExtractor: ComplexExtractor = CourtesyTonesExtractor

  override def indexResult(values: Seq[CourtesyTone]): Result = {
    Ok(views.html.courtesyTones(values))
  }

  override def editResult(filledForm: Form[CourtesyTone], namedKey: NamedKey)(using request: MessagesRequest[AnyContent]): Result =
    given Form[CourtesyTone] = filledForm

    Ok(views.html.courtesyToneEdit(namedKey))

  override def saveOkResult(): Result =
    Redirect(routes.CourtesyToneController.index())

  override val form: Form[CourtesyTone] =
    CourtesyTone.form
}
