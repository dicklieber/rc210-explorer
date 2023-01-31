package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import org.specs2.mutable.Specification

class RC210DownloadSpec extends Specification {

  "RC210Download" should {
    "listPorts" in {
      val ports = RC210Download.listPorts
      ports.foreach{println(_)}

      ports.foreach{cp =>
        val sp = SerialPort.getCommPort(cp.descriptor)
        sp.closePort()
      }


      ok
    }
  }
}
