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

import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.security.authentication.RcSession.SessionId
import org.apache.pekko.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, SupervisorStrategy}
import play.api.libs.concurrent.ActorModule

import java.nio.file.Paths
import javax.inject.Named
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


@Singleton()
object SessionManagerActor extends ActorModule with LazyLogging {
  sealed trait SessionManagerMessage

  type Message = SessionManagerMessage

  case class Create(user: User, ip: String, replyTo: ActorRef[RcSession]) extends SessionManagerMessage

  case class Lookup(sessionId: SessionId, replyTo: ActorRef[Option[RcSession]]) extends SessionManagerMessage

  case class Remove(sessionId: SessionId, replyTo: ActorRef[String]) extends SessionManagerMessage

  case class Sessions(replyTo: ActorRef[Seq[RcSession]]) extends SessionManagerMessage

  case object Tick extends SessionManagerMessage

  @Provides def apply (@Named("vizRc210.sessionFile") sSessionFile:String)
                     (implicit ec: ExecutionContext): Behavior[SessionManagerMessage] =

    Behaviors.withTimers[SessionManagerMessage] { timerScheduler =>
      timerScheduler.startTimerWithFixedDelay(Tick, 5 seconds, 10 seconds)

      Behaviors
        .supervise[Message] {
          Behaviors.setup[SessionManagerMessage] { actorContext =>
            val sessionManager = new SessionManager(Paths.get(sSessionFile))

            Behaviors.receiveMessage[SessionManagerMessage] { message =>
              message match {
                case Create(user: User, ip: String, replyTo: ActorRef[RcSession]) =>
                  val rcSession: RcSession = sessionManager.create(user, ip)
                  replyTo ! rcSession
                case Lookup(sessionId: SessionId, replyTo: ActorRef[Option[RcSession]]) =>
                  val rcSession: Option[RcSession] = sessionManager.lookup(sessionId)
                  replyTo ! rcSession
                case Sessions(replyTo: ActorRef[Seq[RcSession]]) =>
                  replyTo ! sessionManager.sessions
                case Remove(sessionId: SessionId, replyTo: ActorRef[String]) =>
                  sessionManager.remove(sessionId)
                  replyTo ! "Done"
                case Tick =>
                  sessionManager.tick()
              }
              Behaviors.same
            }
              .receiveSignal {
                case (_, signal: Signal) =>
                  logger.error(s"signal: $signal")
                  //              if signal == PreRestart || signal == PostStop =>
                  //          resource.close()
                  Behaviors.same
              }
          }
        }.onFailure[Exception](SupervisorStrategy.restart)
    }
}