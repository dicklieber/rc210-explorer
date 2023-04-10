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

package net.wa9nnn.rc210.serial

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.fazecast.jSerialComm.{SerialPort, SerialPortDataListener, SerialPortEvent}
import com.typesafe.scalalogging.LazyLogging

import java.io.IOException
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.matching.Regex

object RC210Actor extends LazyLogging {
  trait RC210Message

  case class StartDownload(descriptor: String) extends RC210Message

  def create(): Behavior[RC210Message] = {
    logger.info("create()")
    var maybeSerialport: Option[SerialPort] = None

    def write(s: String): Unit = maybeSerialport.foreach {
      val bytes = s.getBytes
      serialPort => serialPort.writeBytes(bytes, bytes.length)
    }

    Behaviors.receiveMessage[RC210Message] {
      case StartDownload(descriptor) =>
        //        replyTo ! s"Hello, $name"
        logger.info(s"Starting: $descriptor")

        /*
                val serialPort: SerialPort = SerialPort.getCommPort(descriptor)
                //    serialPort.setBaudRate(57600)
                serialPort.setBaudRate(19200)

                val open = serialPort.isOpen
                val systemPortName = serialPort.getSystemPortName
                val opened: Boolean = serialPort.openPort()
                if (!opened) {
                  throw new IOException(s"Did not open $serialPort")
                }
                maybeSerialport = Option(serialPort)
                serialPort.addDataListener(new SerialPortDataListener {
                  override def getListeningEvents = {
                    SerialPort.LISTENING_EVENT_DATA_AVAILABLE
                  }

                  override def serialEvent(event: SerialPortEvent): Unit = {

                    //            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    //              return;
                    val receivedData: String = new String(event.getReceivedData)
                    logger.debug(s"Read $receivedData")
                    val parser(sIndex, value)= receivedData

                  }
                })

                for (_ <- 0 to 3) {
                  write("\r")
                  Thread.sleep(200)
                }
                write("1SendEram\r\n")

        */

        //        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0)
        //        val outputStream: OutputStream = serialPort.getOutputStream


        Behaviors.same
    }
  }

  val parser: Regex = """(\d+),(\d+)""".r

}

/**
 *
 * @param descriptor       for [[SerialPort]]
 * @param progress         callback to uipdarte progress bar in client.
 * @param executionContext where this will execute.
 */


case class Progress(n: Int, of: Int = 4096)