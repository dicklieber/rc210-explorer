package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import org.specs2.mutable.Specification

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success, Try}

class RC210IOSpec extends Specification {

  "RC210Download" should {
    "listPorts" in {
      val ports = RC210IO.listPorts
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

object DownloadTest extends App {
  private val ports = RC210IO.listPorts
  private val maybePort: Option[ComPort] = ports.find(_.friendlyName.contains("FT232"))
  private val comport: ComPort = maybePort.get

  private val triedBytes: Try[Array[Int]] = RC210IO.download(comport)
  triedBytes match {
    case Failure(exception) =>
      exception.printStackTrace()
    case Success(result: Array[Int]) =>
      println(s"Read ${result.length} values from $comport")
      val memory = new Memory(result)

      val logsDir = Paths.get("logs")
      val path = Files.createTempFile(logsDir, "Mem", ".txt")

      memory.save(path)
  }
}


object SendResceiveTest extends App {
  println(RC210IO.sendReceive("\r\r1GetVersion\r"))
  println(RC210IO.sendReceive("\r\r1GetRTCVersion\r"))
}
