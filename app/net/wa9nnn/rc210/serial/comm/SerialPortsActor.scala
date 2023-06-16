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

import java.nio.file.{Files, Paths}
import javax.inject.{Named, Singleton}
import scala.concurrent.ExecutionContext

object SerialPortsActor extends ActorModule with LazyLogging {
  sealed trait DataCollectorMessage

  type Message = DataCollectorMessage

  case class SerialPorts(replyTo: ActorRef[Seq[ComPort]]) extends Message


  case class SelectPort(portDescriptor: String) extends Message

  case class CurrentPort(replyTo: ActorRef[Option[ComPort]]) extends Message

  case class SendReceive(request: String, replyTo: ActorRef[Seq[String]]) extends Message

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
  def apply(implicit @Named("vizRc210.serialPortsFile") sFile: String, serialPortsSource: SerialPortsSource, executionContext: ExecutionContext): Behavior[Message] = {
    val file = Paths.get(sFile)

    val dir = file.getParent
    logger.debug(dir.toFile.toString)

    var currentComPort: Option[ComPort] = None
    try {
      selectPort(Files.readString(file))
    } catch {
      case e: Exception =>
        logger.error(e.getMessage)
        None
    }

    def selectPort(portDesctiptor: String): Unit = {
      serialPortsSource()
        .find(_.descriptor == portDesctiptor)
        .foreach { comPort =>
          currentComPort = Option(comPort)
          Files.writeString(file, comPort.descriptor)
        }
    }

    Behaviors.setup { context =>
      Behaviors.supervise[Message] {
        Behaviors.receiveMessage[Message] { message: Message =>
          message match {
            case SerialPorts(replyTo) =>
              replyTo ! serialPortsSource()
            case SelectPort(portDesctiptor) =>
              selectPort(portDesctiptor)
            case CurrentPort(replyTo) =>
              replyTo ! currentComPort
            case SendReceive(request, replyTo) =>

            /*currentComPort.map { comPort =>
              Using(new RequestResponse(comPort)) { requestResponse: RequestResponse =>
                requestResponse.perform(request).map {
                  replyTo ! _
                }
              }
            }*/
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
    ports.map(ComPort(_))
      .filterNot(_.descriptor.contains("/tty")) // just want tty, callin, devices. Leaves COM alone
      .toList
  }
}