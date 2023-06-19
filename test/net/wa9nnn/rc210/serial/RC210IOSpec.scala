package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.serial.comm.RcOperation

import scala.language.postfixOps



object RcOperationTest extends App {

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

  private val requestResponse = new RcOperation(ft232Port)

  try {
    val response: Seq[String] = requestResponse.perform("1GetVersion")
    println(response)
  } finally
    requestResponse.close()
}


