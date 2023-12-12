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
import net.wa9nnn.rc210.data.datastore.{AllForKeyKind, DataStore, DataStoreMessage, DataStoreReply, UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey, SimpleFieldValue}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.SimpleValuesHandler
import play.api.mvc.*

import javax.inject.{Inject, Singleton}

@Singleton
class CommonEditorController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  private var simpleValuesHandler: Option[SimpleValuesHandler] = None

  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>

      val fieldEntries = dataStore(AllForKeyKind(KeyKind.commonKey)).all
      if (simpleValuesHandler.isEmpty)
        simpleValuesHandler = Some(new SimpleValuesHandler(fieldEntries))
      Ok(views.html.candidates(fieldEntries))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      dataStore(simpleValuesHandler.get.collect)
      Redirect(routes.CommonEditorController.index())
  }
}

