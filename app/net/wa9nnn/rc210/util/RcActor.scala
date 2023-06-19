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

package net.wa9nnn.rc210.util

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{Behavior, Signal, SupervisorStrategy}
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.concurrent.ActorModule

/**
 * Common behavior for actors in this application.
 * Subclasses should handle dependencies in an apply method and implement specific message handling within [[behavior()]] callback.
 *
 * @tparam M sealed trait for messages handled by the actor/
 */
abstract class RcActor extends ActorModule with LazyLogging {

  def behavior[M](onMessage: M => Unit): Behavior[M] = {
    Behaviors.supervise[M] {
      Behaviors.receiveMessage[M] { message: M =>
        onMessage(message)
        Behaviors.same
      }
        .receiveSignal {
          case (context: ActorContext[M], signal: Signal) =>
            logger.error(s"signal: $signal")
            //              if signal == PreRestart || signal == PostStop =>
            //          resource.close()
            Behaviors.same
        }
    }
      .onFailure[Exception](SupervisorStrategy.restart)
  }
}
