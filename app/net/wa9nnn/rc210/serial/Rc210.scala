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
import com.wa9nnn.util.tableui.{Header, Row, Table}
import configs.syntax._
import net.wa9nnn.rc210.serial.comm.{Rc210Version, RcEventBased, RcSerialPort, RcStreamBased}

import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Try, Using}


@Singleton
class Rc210 @Inject()(config: Config) extends LazyLogging {
  def comPort: ComPort = maybeRcSerialPort.map(_.comPort).get


  private val serialPortsSource = new SerialPortsSource()

  private val file: Path = config.get[Path]("vizRc210.serialPortsFile").value

  Files.createDirectories(file.getParent)
  private var maybeRcSerialPort: Option[RcSerialPort] = None
  selectPort(Files.readString(file))

  implicit def serialPort: RcSerialPort = {
    maybeRcSerialPort.getOrElse(throw NoPortSelected())
  }

  def sendOne(request: String): RcOperationResult = {
    RcOperationResult(request, Using(openStreamBased) { rcOp =>
      rcOp.perform(request)
    })
  }

  def sendBatch(name: String, requests: String*): BatchOperationsResult = {
    BatchOperationsResult(name, Using.resource(openStreamBased) { rcOp: RcStreamBased =>
      requests.map { request =>
        RcOperationResult(request, Try(rcOp.perform(request)))
      }
    })
  }

  def openStreamBased: RcStreamBased = {
    new RcStreamBased(serialPort)
  }

  def openEventBased(): RcEventBased = {
    new RcEventBased(serialPort)
  }


  def selectPort(portDescriptor: String): Unit =
    try {
      serialPortsSource().find(_.descriptor == portDescriptor)
        .foreach { comPort =>
          Files.writeString(file, comPort.descriptor)
          val rcSerialPort = RcSerialPort(SerialPort.getCommPort(comPort.descriptor))
          try {
            maybeRcSerialPort = Option(rcSerialPort) // may get overridden if we have a version
            val rcOperationResult = sendOne("1GetVersion")
            val rawVersion = rcOperationResult.head
            maybeRcSerialPort = Option(rcSerialPort.withVersion(Rc210Version(rawVersion, comPort)))
          } catch {
            case e: Exception =>
              logger.error(s"Getting Version from RC210", e)
          }
        }
    } catch {
      case e: Exception =>
        logger.error(s"Selecting $portDescriptor", e)
    }

  def table(): Table = {
    val currentComPort: Option[ComPort] = maybeRcSerialPort.map(_.comPort)
    val rows: Seq[Row] = serialPortsSource().map { comPort: ComPort =>
      var row = comPort.toRow
      if (currentComPort.contains(comPort)) {
        row = row.withCssClass("selected")
        for {
          asp <- maybeRcSerialPort
          ver <- asp.maybeVersion
        } {
          row = row :+ ver.toCell.withToolTip("Version reported by RC210.")
        }
      }
      row
    }

    val table = Table(Header("Serial Ports", "Descriptor", "Friendly Name"), rows)
    table
  }


}


