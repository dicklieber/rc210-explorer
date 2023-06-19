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

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import com.fazecast.jSerialComm.SerialPort
import com.google.inject.{Inject, Provides}
import net.wa9nnn.rc210.serial.comm.RcOperationsActor.SendReceive
import net.wa9nnn.rc210.serial.{ComPort, ComPortPersistence}
import net.wa9nnn.rc210.util.RcActor

import javax.inject.Singleton
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Try}

object RcOperationsActor extends RcActor {
  type Message = DataCollectorMessage

  sealed trait DataCollectorMessage

  //  type Message = DataCollectorMessage

  case class SerialPorts(replyTo: ActorRef[Seq[ComPort]]) extends Message


  case class SelectPort(portDescriptor: String) extends Message

  case class CurrentPort(replyTo: ActorRef[Option[ComPort]]) extends Message

  case class SendReceive(request: String, replyTo: ActorRef[RcOperationResult]) extends Message


  /**
   *
   * @param comPortPersistence           current [[ComPort]]
   * @param serialPortsSource            gets serial port list.
   * @return Behavior for this actor.
   */

  @Provides
  def apply(comPortPersistence: ComPortPersistence, serialPortsSource: SerialPortsSource): Behavior[Message] = {
    behavior[Message] {
      case SerialPorts(replyTo) =>
        replyTo ! serialPortsSource().sorted
      case SelectPort(portDesctiptor) =>
        comPortPersistence.selectPort(portDesctiptor)
      case CurrentPort(replyTo) =>
        replyTo ! comPortPersistence.currentComPort
      case SendReceive(request, replyTo) =>
        replyTo ! (comPortPersistence.currentComPort match {
          case Some(comPort: Any) =>
            val result = RcOperation(request, comPort)
            result
          case None =>
            RcOperationResult(request, Failure(NoPortAvailable))
        })
    }

  }
}

@Singleton
class RcHelper @Inject()(implicit rcOperationsActor: ActorRef[RcOperationsActor.Message], scheduler: Scheduler) {
  implicit val timeout: Timeout = 2 seconds

  def apply(request: String): RcOperationResult = Await.result(rcOperationsActor.ask(SendReceive(request, _)), 2 second)
}

@Singleton
class SerialPortsSource() {
  def apply(): Seq[ComPort] = {
    val ports = SerialPort.getCommPorts
    ports.map(ComPort(_))
      .filterNot(_.descriptor.contains("/tty")) // just want tty, callin, devices. Leaves COM alone
      .toList
  }
}

case object NoPortAvailable extends Exception("No Serial Port Available")