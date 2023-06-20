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
import com.google.inject.Provides
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.ComPort
import play.api.libs.concurrent.ActorModule

import javax.inject.{Named, Singleton}
import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import scala.concurrent.{ExecutionContext, Future}

object DataCollectorActor extends ActorModule with LazyLogging {
  sealed trait DataCollectorMessage

  type Message = DataCollectorMessage

  case class StartDownload(descriptor:String) extends Message

  case class ProgressRequest(replyTo: ActorRef[Progress]) extends Message


  @Provides
  def apply (implicit @Named("vizRc210.memoryFile") sMemoryFile: String, executionContext: ExecutionContext): Behavior[Message] = {
    val path = Paths.get(sMemoryFile)
    Files.createDirectories(path.getParent)
    val temp = path.resolveSibling(sMemoryFile + ".temp")
    /**
     * DataCollectorStuff while running
     * Progress final progress when completed.
     */
    var state: Either[Progress, ProgressSource] = Left(Progress())

    Behaviors.setup { context =>
      Behaviors.supervise[Message] {
        Behaviors.receiveMessage[Message] { message: Message =>
          message match {
            case StartDownload(descriptor) =>
              val printWritter: PrintWriter = new PrintWriter(Files.newBufferedWriter(temp))
              val collectorStuff = DataCollector(printWritter, descriptor)
              collectorStuff.future.map { finalProgress =>
                state = Left(finalProgress)
              }
              state = Right(collectorStuff.progressSource)
            case ProgressRequest(replyTo) =>
              val p: Progress = state match {
                case Left(progress: Progress) =>
                  progress
                case Right(progressSource: ProgressSource) =>
                  progressSource()
              }
              replyTo ! p
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