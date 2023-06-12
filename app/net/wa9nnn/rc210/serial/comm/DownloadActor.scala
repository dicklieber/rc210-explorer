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
import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.ComPort

import scala.concurrent.{Future, Promise}
import scala.util.matching.Regex

/**
 * DownloadActor full EEROM dump from RC210.
 */
object DownloadActor extends LazyLogging {
  sealed trait DownloadMessage

  type Message = DownloadMessage

  case class ProgressReqest(replyTp:ActorRef[Progress]) extends DownloadMessage

  private val parser: Regex = """(\d+),(\d+)""".r


//  def apply(comPort: ComPort): Behavior[DownloadMessage] = {
//    Behaviors.receive[DownloadMessage] { (context, message) =>
//
//    }


//    def progress:Progress = {
//      val double = count * 100.0 / expectedInts
//      Progress(serialPort.isOpen, f"$double%2.1f%%")
//
//    }


}





