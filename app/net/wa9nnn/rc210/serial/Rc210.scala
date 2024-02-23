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
import com.github.andyglow.config.*
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.serial.comm.*
import net.wa9nnn.rc210.util.Configs
import net.wa9nnn.rc210.util.Configs.*
import os.*

import java.time.Duration
import javax.inject.{Inject, Singleton}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Try, Using}

@Singleton
class Rc210 @Inject()(config: Config, serialPortsSource: SerialPortsSource) extends LazyLogging:
  private var _portAndVersion: PortAndVersion = PortAndVersion()

  private val file: Path = config.get[Path]("vizRc210.serialPortsFile")
  if (os.exists(file))
    selectPort(os.read(file))

  def openStreamBased: RcStreamBased = {
    _portAndVersion.selectedSerialPort.map { serialPort =>
      new RcStreamBased(serialPort)
    }.getOrElse(throw new IllegalStateException("No serial port selected!"))
  }

  def openEventBased(): RcEventBasedOp = {
    _portAndVersion.selectedSerialPort.map { serialPort =>
      new RcEventBasedOp(serialPort)
    }.getOrElse(throw new IllegalStateException("No serial port selected!"))

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

  def isSelected(candidate: SerialPort): Boolean =
    (for {
      port <- _portAndVersion.selectedSerialPort
      systemPortName = port.getSystemPortName
      if candidate.getSystemPortName == systemPortName
    } yield {
      true
    }).getOrElse(false)

  def selectPort(candidate: String): Unit =
    serialPortsSource()
      .find(_.getSystemPortName == candidate)
      .foreach((serialPort: SerialPort) =>
        val ver: Option[String] = Version(serialPort).toOption
        _portAndVersion = PortAndVersion(Option(serialPort), ver))

  def listPorts(): Seq[SerialPort] =
    serialPortsSource()

  def selectedPortInfo: String =
    _portAndVersion.selectedInfo

  def portAndVersion: PortAndVersion =
    _portAndVersion

  object PortAndVersion:
    def apply(serialPort: SerialPort): PortAndVersion =
      new PortAndVersion(Option(serialPort))

  case class PortAndVersion(
                             selectedSerialPort: Option[SerialPort] = None,
                             version: Option[String] = None):

    def selectedInfo: String =
      selectedSerialPort.map { serialPort =>
        s"${serialPort.getDescriptivePortName} v:${version.getOrElse("?")}"
      }.getOrElse("No port selected!")






