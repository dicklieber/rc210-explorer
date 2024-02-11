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
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue, SimpleFieldValue}
import net.wa9nnn.rc210.ui.{NavMainController, SimpleValuesHandler}
import play.api.mvc.*
import net.wa9nnn.rc210.security.Who.*

import javax.inject.{Inject, Singleton}
import net.wa9nnn.rc210.security.Who.given
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.nav.TabKind

@Singleton
class CommonController @Inject()(dataStore: DataStore, components: ControllerComponents)
  extends NavMainController(components)  {
  private var simpleValuesHandler: Option[SimpleValuesHandler] = None

  def index: Action[AnyContent] = Action {
    implicit request =>

      val fieldEntries: Seq[FieldEntry] = dataStore(KeyKind.Common)
      if (simpleValuesHandler.isEmpty)
        simpleValuesHandler = Some(new SimpleValuesHandler(fieldEntries))
      Ok(navMain(KeyKind.Common, views.html.common(fieldEntries)))
  }

  def save(): Action[AnyContent] = Action {
    implicit request =>
      given RcSession = request.attrs(sessionKey)

      dataStore.update(simpleValuesHandler.get.collect)
      Redirect(routes.CommonController.index)
  }
}

