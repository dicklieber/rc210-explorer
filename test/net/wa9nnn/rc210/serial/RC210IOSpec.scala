package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.config.ConfigFactory
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import org.specs2.mutable.Specification

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success, Try}

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
      ok
    }

  }
}

object SerialPortOperationTest extends App {
  private val rc210Io = new RC210IO(ConfigFactory.load())
  private val ports = rc210Io.listPorts
  private val maybePort: Option[ComPort] = ports.find(_.friendlyName.contains("FT232"))
  private val comport: ComPort = maybePort.get


  private val serialPortOperation: SerialPortOperation = rc210Io.start(comport)

  private val triedResponse: Try[String] = serialPortOperation.preform("\r\r1333444555\r")

  triedResponse match {
    case Failure(exception) =>
      exception.printStackTrace()
    case Success(result: String) =>
      println(s"Read ${result.length} values from $comport")
  }
  serialPortOperation.close()
}


//object SendResceiveTest extends App {
//  println(RC210IO.sendReceive("\r\r1GetVersion\r"))
//  println(RC210IO.sendReceive("\r\r1GetRTCVersion\r"))
//}
