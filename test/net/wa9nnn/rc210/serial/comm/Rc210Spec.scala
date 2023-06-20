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
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse
import net.wa9nnn.rc210.serial.{ComPort, ComPortPersistence}
import org.mockito.Mockito.when
import org.scalatest.TryValues
import org.scalatestplus.mockito.MockitoSugar

import scala.util.Try

class Rc210Spec extends WithTestConfiguration with MockitoSugar with TryValues{

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


  "RC210" should {
    val comPortPersistence = mock[ComPortPersistence]
    when(comPortPersistence.currentComPort).thenReturn(Option(ft232Port))
    val rc210 = new Rc210(comPortPersistence)
    "handle one shot" in {
      val triedOperationsResult: Try[OperationsResult] = rc210.sendOne("Version", "1GetVersion")
      val operationsResult: OperationsResult = triedOperationsResult.get
      val rcRes: Seq[RcOperationResult] = operationsResult.results
      rcRes should have length (1)
      val rcOperationResult: RcOperationResult = rcRes.head
      val rcResponse: RcResponse = rcOperationResult.triedResponse.success.value
      rcResponse.head should equal("803")
      rcResponse(1) should equal("+GETVE")

    }
    "handle batch" in {
      val triedOperationsResult: Try[OperationsResult] = rc210.send("Version", Seq("1GetVersion", "1GetVersion"))
      val operationsResult: OperationsResult = triedOperationsResult.get
      val rcRes: Seq[RcOperationResult] = operationsResult.results
      rcRes should have length (2)
      val rcOperationResult: RcOperationResult = rcRes.head
      val rcResponse: RcResponse = rcOperationResult.triedResponse.success.value
      rcResponse.head should equal("803")
      rcResponse(1) should equal("+GETVE")

    }

  }

}
