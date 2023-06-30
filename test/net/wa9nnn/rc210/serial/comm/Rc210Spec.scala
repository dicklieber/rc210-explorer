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
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse
import net.wa9nnn.rc210.serial.{ComPort, RcSerialPort, RcSerialPortManager}
import org.mockito.Mockito.when
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.{Sequential, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock

import scala.util.{Failure, Success}

class Rc210Spec extends Sequential(
  new SendOne(),
  new SendBatch(),
  new StartStopClose(),
) with MockitoSugar with TryValues {
}

class SendOne extends RcSpec {
  val rcResponseSendOne: RcResponse = Rc210ForTest().sendOne("Version", "1GetVersion").triedResponse.success.value
  rcResponseSendOne.head should equal("803")
  rcResponseSendOne(1) should equal("+GETVE")
}

class SendBatch extends RcSpec {
  val batchRespnses: Seq[RcOperationResult] = Rc210ForTest().sendBatch("Version", "1GetVersion", "1GetVersion").get.results
  batchRespnses should have length (2)
  val rcOperationResult: RcOperationResult = batchRespnses.head
  val rcResponse: RcResponse = rcOperationResult.triedResponse.success.value
  rcResponse.head should equal("803")
  rcResponse(1) should equal("+GETVE")
  println("sendBatch done")
}

class StartStopClose extends RcSpec {
  Rc210ForTest().start match {
    case Failure(exception) =>
      throw exception
    case Success(rcOperation: RcOperation) =>
      val r1 = rcOperation.sendBatch("one", "1GetVersion", "1GetVersion")
      val r2 = rcOperation.sendBatch("two", "1GetVersion", "1GetVersion")

      r1.name should be("one")
      r2.name should be("two")
      rcOperation.close()
  }
}

object Rc210ForTest {
  def apply(): Rc210 = {
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

    val rcSerialPortManager = mock[RcSerialPortManager]
    val serialPort = SerialPort.getCommPort(ft232Port.descriptor)
    val rcSerialPort = new RcSerialPort(serialPort)
    when(rcSerialPortManager.serialPort()).thenReturn(rcSerialPort)
    new Rc210(rcSerialPortManager)

  }
}
