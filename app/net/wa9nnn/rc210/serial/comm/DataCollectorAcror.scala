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

package net.wa9nnn.rc210.serial.comm
import akka.actor.typed.{ActorRef, Behavior, Signal, SupervisorStrategy}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.concurrent.ActorModule

object DataCollectorActor extends ActorModule with LazyLogging {
  sealed trait DataCollectorMessage

  type Message = DataCollectorMessage

  case object StartDownload extends Message

  case class ProgressRequest(replyTo: ActorRef[Progress]) extends Message

  case class RC210Result(mainArray: Seq[Int], extArray: Seq[Int], progress: Progress) extends Message

  def apply(): Behavior[Message] = {

    Behaviors.setup { context =>
      Behaviors.supervise[Message] {
        Behaviors.receiveMessage[Message] { message: Message =>
          message match {
            case StartDownload => ???
            case ProgressRequest(replyTo) => ???
            case RC210Result(mainArray, extArray, progress) => ???
          }
          Behaviors.same
        }
          .receiveSignal {
            case (context: ActorContext[Message], signal: Signal) =>
              logger.error(s"signal: $signal")
              //              if signal == PreRestart || signal == PostStop =>
              //          resource.close()
              Behaviors.same
          }
      }
        .onFailure[Exception](SupervisorStrategy.restart)
    }
  }
}