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
import com.wa9nnn.wa9nnnutil.tableui.Table
import net.wa9nnn.rc210.security.authentication.{RcSession, SessionStore}
import net.wa9nnn.rc210.ui.Tabs
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import views.html.justdat

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

/**
 * View Sessions
 */
@Singleton
class SessionController @Inject()(sessionStore: SessionStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {

  def index: Action[AnyContent] = Action {
    val sessions = sessionStore.sessions
    val rows = sessions.map(_.toRow)
    val table: Table = Table(RcSession.header(rows.length), rows)
    Ok(justdat(Tabs.security,  Seq(table)))
  }
}
