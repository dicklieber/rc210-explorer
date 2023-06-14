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

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Signal, SupervisorStrategy}
import com.fazecast.jSerialComm.SerialPort
import com.google.inject.Provides
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.ComPort
import play.api.libs.concurrent.ActorModule

import javax.inject.{Named, Singleton}

object SerialPortsActor extends ActorModule with LazyLogging {
  sealed trait DataCollectorMessage

  type Message = DataCollectorMessage

  case class SerialPorts(replyTo: ActorRef[Seq[ComPort]]) extends Message

  case class SelectPort(comPort: ComPort) extends Message

  case class CurrentPort(replyTo: ActorRef[Option[ComPort]]) extends Message

  /*  val f: () => Array[SerialPort] = SerialPort.getCommPorts _
    def ddd(f:() => Array[SerialPort]): Unit {

    }

    private val unit: Unit = ddd(f)*/

  /**
   *
   * @param file           where to store currently selected port.
   * @param comPortsSource gets serial port list.
   * @return Behavior for this actor.
   */

  @Provides
  def apply(@Named("vizRc210.serialPortsFile") file: String, comPortsSource: SerialPortsSource): Behavior[Message] = {
    var currentComPort: Option[ComPort] = None

    Behaviors.setup { context =>
      Behaviors.supervise[Message] {
        Behaviors.receiveMessage[Message] { message: Message =>
          message match {
            case SerialPorts(replyTo) =>
              replyTo ! comPortsSource()
            case SelectPort(comPort) =>
              println(comPort)

            case CurrentPort(replyTo) =>
              println(replyTo)
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

@Singleton
class SerialPortsSource() {
  def apply(): Seq[ComPort] = {
    val ports = SerialPort.getCommPorts
    ports.map(ComPort(_)).toList
  }
}