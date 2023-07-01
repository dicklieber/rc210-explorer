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

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import controllers.routes
import net.wa9nnn.rc210.serial.comm.{BatchOperationsResult, Rc210, RcOperationResult, SerialPortsSource}

import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class CurrentSerialPort @Inject()(serialPortsSource: SerialPortsSource) extends LazyLogging {
  private var _currentPort: Option[RcSerialPort] = None

  def currentPort: RcSerialPort = _currentPort.getOrElse(throw new NoPortSelected)

  def setCurrentPort(newSerialPort: RcSerialPort): Unit = {
    _currentPort = Option(newSerialPort)
  }

  def table(rc210: Rc210): Table = {
    _currentPort.foreach { rcSerialPort =>
      if (rcSerialPort.rcVersion.isEmpty) {
        try {
          val rcOperationResult: RcOperationResult = rc210.sendOne("1GetVersion")
          val sVersion: String = rcOperationResult.head
          val formatted = s"${sVersion.head}.${sVersion.tail}"
          _currentPort = Option(rcSerialPort.withVersion(formatted))
        } catch {
          case e: Exception =>
            logger.debug("No response or don't response from serial port. Prosabaly not an RC-210.")
        }
      }
    }


    val rows: Seq[Row] = serialPortsSource().map { comPort: ComPort =>
      var row = comPort.toRow
      _currentPort.foreach { selected =>
        if (selected.comPort == comPort) {
          row = row.withCssClass("selected")
          selected.rcVersion.foreach { ver =>
            row = row :+ ver
          }
        }
      }
      row
    }
    val table = Table(Header("Serial Ports", "Descriptor", "Friendly Name", "RC210 Version"), rows)
    table
  }
}
