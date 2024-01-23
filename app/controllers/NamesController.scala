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
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.datastore.{CandidateAndNames, DataStore, UpdateCandidate}
import net.wa9nnn.rc210.data.remotebase.RemoteBaseNode
import net.wa9nnn.rc210.security.Who.request2Session
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import play.api.data.Forms.*
import play.api.data.{Field, Form, Mapping}
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap

@Singleton()
class NamesController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging:
  /**
   * Show all defined names.
   *
   * @return
   */
  def index: Action[AnyContent] = Action {
    implicit request =>
      val table = Table(Header("Names",
        Cell("Key")
          .withToolTip("Keys are, usually, RC-210 data that are numbered."),
        "Name"
      ),
        dataStore.namedKeys.map(_.toRow)
      )

      Ok(views.html.names(table))
  }





