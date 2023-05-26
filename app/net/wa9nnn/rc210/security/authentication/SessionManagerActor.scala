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

package net.wa9nnn.rc210.security.authentication

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.google.inject.{Provides, Singleton}
import net.wa9nnn.rc210.security.authentication.RcSession.SessionId
import play.api.libs.concurrent.ActorModule

import javax.inject.Inject
import scala.collection.concurrent.TrieMap


@Singleton()
object SessionManagerActor extends ActorModule {
  trait SessionManagerMessage
  type Message = SessionManagerMessage

  case class Create(user: User, ip: String, replyTo: ActorRef[RcSession]) extends SessionManagerMessage

  @Provides def apply(sessionManager: SessionManager): Behavior[SessionManagerMessage] = {
    // TODO: Define ConfiguredActor's behavior using the injected configuration.
    Behaviors.receiveMessage {
      case Create(user, ip, replyTo) =>
        val rcSession = sessionManager.create(user, ip)
        replyTo ! rcSession
        Behaviors.same
    }
  }


}