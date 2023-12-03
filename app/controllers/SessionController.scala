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

import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.security.authentication.SessionManagerActor.Sessions
import net.wa9nnn.rc210.security.authentication.{RcSession, SessionManagerActor}
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents, MessagesInjectedController}
import views.html.justdat

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
 * User Management
 */
@Singleton
class SessionController @Inject()(actor: ActorRef[SessionManagerActor.SessionManagerMessage])
                                 (implicit scheduler: Scheduler, ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index: Action[AnyContent] = Action.async {

    val future: Future[Seq[RcSession]] = actor.ask[Seq[RcSession]](ref => Sessions(ref))
    future.map { sessions =>
      val rows = sessions.map(_.toRow)
      val table: Table = Table(RcSession.header(rows.length), rows)
      Ok(justdat(Seq(table)))

    }

  }
}
