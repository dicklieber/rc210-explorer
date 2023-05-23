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

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.authentication.{RcSession, SessionManager, UserManager, UserRecords}
import net.wa9nnn.rc210.security.authorzation.AuthFilter
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.mvc.{Action, AnyContent, MessagesInjectedController, MessagesRequest}
import views.html.dat

import javax.inject.{Inject, Singleton}

/**
 * User Management
 */
@Singleton
class SessionController @Inject()(implicit sessionManager: SessionManager)
  extends MessagesInjectedController with LazyLogging {


  def index: Action[AnyContent] = Action {
    val rows = sessionManager.sessions.map(_.toRow)
    val table = Table(RcSession.header(rows.length), rows)
    Ok(dat(Seq(table)))
  }

}
