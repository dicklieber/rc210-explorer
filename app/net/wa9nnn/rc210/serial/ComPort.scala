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

case class ComPort(descriptor: String = "com1", friendlyName: String = "com1") extends Ordered[ComPort] {
  override def toString: String = s"$descriptor/$friendlyName"

  override def compare(that: ComPort): Int = friendlyName compareTo (that.friendlyName)
}

object ComPort {
    def apply(serialPort: SerialPort): ComPort = {
    new ComPort(serialPort.getSystemPortPath, serialPort.getDescriptivePortName)
  }
  def apply(fromToString:String): ComPort = {
    val tokens = fromToString.split("/")
    new ComPort(tokens.head, tokens(1))
  }
}