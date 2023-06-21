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

import com.fazecast.jSerialComm.SerialPort
import net.wa9nnn.RcSpec
import net.wa9nnn.rc210.serial.ComPort
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse
import org.scalatest.TryValues

import scala.util.Try

class RcOperationTest extends RcSpec with TryValues{
  def listPorts: Seq[ComPort] = {
    val ports = SerialPort.getCommPorts
    ports.map(ComPort(_)).toList
  }

  def ft232Port: ComPort = {
    listPorts.find(_.friendlyName.contains("FT232")) match {
      case Some(value) =>
        value
      case None =>
        throw new IllegalStateException("Can't find FT232 port")
    }
  }

  "testPerform" in {
    val rcOperation = new RcOperation(ft232Port)
    try {
      val result: RcOperationResult = rcOperation.sendOne("1GetVersion")
      val triedRiedResponse: Try[RcResponse] = result.triedResponse

      val rcRes: RcResponse = triedRiedResponse.success.value

      rcRes should have length (2)
      rcRes.head should equal("803")
      rcRes(1) should equal("+GETVE")
    } finally{
      rcOperation.close()
    }
  }
  "single shot" in {
    val tried: RcOperationResult = RcOperation("1GetVersion", ft232Port)

    val response = tried.triedResponse.success.value

    response should have length (2)
    response.head should equal("803")
    response(1) should equal("+GETVE")

  }
}
