package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.serial.comm.RequestResponse

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class RC210IOSpec extends WithTestConfiguration {

  private val rc210Io = new RC210IO(config)
  "RC210Download" should {
    "listPorts" in {
      val ports =
        rc210Io.listPorts
      ports.foreach {
        println(_)
      }

      ports.foreach { cp =>
        val sp = SerialPort.getCommPort(cp.descriptor)
        sp.closePort()
      }
    }
  }
}

object SerialPortOperationTest extends App {

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

  private val requestResponse = new RequestResponse(ft232Port)

  try {
     val response: Seq[String] = requestResponse.perform("1GetVersion")
    println(response)
  } finally
    requestResponse.close()
}


//object SendResceiveTest extends App {
//  println(RC210IO.sendReceive("\r\r1GetVersion\r"))
//  println(RC210IO.sendReceive("\r\r1GetRTCVersion\r"))
//}
