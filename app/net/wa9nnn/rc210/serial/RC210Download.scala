package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort

case object RC210Download {
  def listPorts: List[ComPort] = {
    SerialPort.getCommPorts.map(ComPort(_)).toList
  }
}

case class ComPort(descriptor: String, friendlyName: String)

object ComPort {
  def apply(serialPort: SerialPort): ComPort = {
    new ComPort(serialPort.getSystemPortPath, serialPort.getDescriptivePortName)
  }
}
