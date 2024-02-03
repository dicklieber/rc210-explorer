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

import com.fazecast.jSerialComm.{SerialPort, SerialPortDataListener}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.serial.comm.*
import net.wa9nnn.rc210.util.Configs

import java.nio.file.{Files, Path}
import java.time.Duration
import javax.inject.{Inject, Singleton}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Try, Using}

@Singleton
class Rc210 @Inject()(implicit config: Config) extends LazyLogging:

  //  def maybeSelectedComPort: ComPort = maybeRcSerialPort.map(_.comPort).get

  private val serialConfig = SerialConfig(config)

  private val serialPortsSource = new SerialPortsSource()

  private val file: Path = Configs.path("vizRc210.serialPortsFile")

  Files.createDirectories(file.getParent)
  private var maybeRcSerialPort: Option[RcSerialPort] = None
  if (Files.exists(file))
    selectPort(Files.readString(file))

  def openStreamBased: RcStreamBased = {
    new RcStreamBased(rcSerialPort())
  }

  def rcSerialPortOption: Option[RcSerialPort] = maybeRcSerialPort

  def rcSerialPort(): RcSerialPort =
    maybeRcSerialPort.getOrElse(throw new IllegalStateException("No serial port selected!"))

  def openEventBased(): RcEventBasedOp = {
    new RcEventBasedOp(rcSerialPort())
  }

  def sendOne(request: String): RcOperationResult = {
    RcOperationResult(request, Using(openStreamBased) { rcOp =>
      rcOp.perform(request)
    })
  }

  def sendBatch(requests: String*): Seq[RcOperationResult] =
    Using.resource(openStreamBased) { (rcOp: RcStreamBased) =>
      requests.map { request =>
        RcOperationResult(request, Try(rcOp.perform(request)))
      }
    }

  def checkedFlag(candidate: SerialPort): String =
    (for {
      rcSerialPort <- maybeRcSerialPort
      bool = rcSerialPort.serialPort.getSystemPortName == candidate.getSystemPortName
      if bool
    }
    yield
      logger.info("checked")
      "checked"
      ).getOrElse("")

  def selectPort(candidate: String): Unit =
    try {
      serialPortsSource.apply().find(_.getSystemPortName == candidate)
        .foreach { serialPort =>
          Files.writeString(file, serialPort.getSystemPortName)
          val rcSerialPort = RcSerialPort(serialPort)
          try {
            maybeRcSerialPort = Option(rcSerialPort)
          } catch {
            case e: Exception =>
              logger.error(s"Getting Version from RC210", e)
          }
        }
    } catch {
      case e: Exception =>
        logger.error(s"Selecting $candidate", e)
    }

  def listPorts(): Seq[SerialPort] =
    serialPortsSource()

