package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.config.ConfigFactory
import javafx.util.Duration.seconds
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.serial.comm.RequestResponse
import org.specs2.mutable.Specification

import java.nio.file.{Files, Paths}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
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

  private val serialPortOperation = new RequestResponse(ft232Port)

  try {
     val eventualString: Future[Seq[String]] = serialPortOperation.perform("1GetVersion")
     val response: Seq[String] = Await.result[Seq[String]](eventualString, 20 seconds)

    println(response)
  } finally
    serialPortOperation.close()
}


//object SendResceiveTest extends App {
//  println(RC210IO.sendReceive("\r\r1GetVersion\r"))
//  println(RC210IO.sendReceive("\r\r1GetRTCVersion\r"))
//}
