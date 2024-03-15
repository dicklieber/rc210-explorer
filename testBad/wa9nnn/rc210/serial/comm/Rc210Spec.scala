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

import com.typesafe.config.ConfigFactory
import org.scalatest.{Sequential, TryValues}
import org.scalatestplus.mockito.MockitoSugar

import scala.util.{Failure, Success, Try}

class Rc210Spec extends RcSpec with  MockitoSugar with TryValues {
  val rc210 = new Rc210(ConfigFactory.load())
  new Sequential(
    new SendOne(),
    new SendBatch(),
    new StartStopClose(),
  )


  class SendOne extends RcSpec {
    "first" in {
      val rcResponseSendOne: RcResponse = rc210.sendOne("1GetVersion").triedResponse.success.value
      rcResponseSendOne.head should equal("803")
      rcResponseSendOne.lines(1) should equal("+GETVE")
    }
    "second" in {
      val rcResponseSendOne: RcResponse = rc210.sendOne("1GetVersion").triedResponse.success.value
      rcResponseSendOne.head should equal("803")
      rcResponseSendOne.lines(1) should equal("+GETVE")
    }
  }

  class SendBatch extends RcSpec {
    val batchRespnses: Seq[RcOperationResult] = rc210.sendBatch("Version", "1GetVersion", "1GetVersion").results
    batchRespnses should have length 2
    val rcOperationResult: RcOperationResult = batchRespnses.head
    private val triedResponse: Try[RcResponse] = rcOperationResult.triedResponse
    triedResponse match {
      case Failure(exception) =>
        throw exception
      case Success(value: RcResponse) =>
        val rcResponse: RcResponse = triedResponse.success.value
        rcResponse.head should equal("803")
        rcResponse.lines(1) should equal("+GETVE")
        println("sendBatch done")
    }


  }

  class StartStopClose extends RcSpec {
    val rcOperation: RealStreamBased = rc210.openStreamBased
    private val r1 = rcOperation.perform(Seq("1GetVersion", "1GetVersion"))
    private val r2 = rcOperation.perform(Seq("1GetVersion", "1GetVersion"))

    r1.name should be("one")
    r2.name should be("two")
    rcOperation.close()
  }

}


