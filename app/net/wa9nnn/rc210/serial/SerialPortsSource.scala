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

import com.fazecast.jSerialComm.SerialPort

import javax.inject.Singleton
import scala.language.postfixOps

@Singleton
class SerialPortsSource() {
  /**
   * All the serial ports.
   *
   * @return serial portw as [[ComPort]]
   */
  def apply(): Seq[SerialPort] =
    SerialPort.getCommPorts
      .filterNot { pred =>
        val systemPortName = pred.getSystemPortName
        systemPortName.startsWith("tty")
      } // just want call-in, devices. Leaves COM alone
      .toList
}


