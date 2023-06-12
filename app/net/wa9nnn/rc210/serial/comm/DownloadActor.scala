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

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.RC210Result
import net.wa9nnn.rc210.serial.{ComPort, comm}

/**
 * DownloadActor full EEROM dump from RC210.
 */
object DownloadActor extends LazyLogging {
  sealed trait DownloadMessage

  type Message = DownloadMessage

  case class ProgressReqest(replyTp: ActorRef[Progress]) extends DownloadMessage


  def apply(portDescriptor:String, actorRef: ActorRef[DataStoreActor.Message]): Behavior[DownloadMessage] = {

    val collector = new DataCollector(portDescriptor)
    //    collector.future.mapTo()
    Behaviors.receiveMessage[DownloadMessage] { message =>
      message match {
        case ProgressReqest(replyTo) =>
          replyTo ! collector.progress
        case r: RC210Result =>
          // send to DSA
      }
      Behaviors.same
    }


  }
}





