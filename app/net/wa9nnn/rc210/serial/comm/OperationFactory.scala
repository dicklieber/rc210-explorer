package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.{SerialPort, SerialPortDataListener}




trait OperationFactory {
  def openStreamBased(serialPort: SerialPort): StreamBased

  def openEventBased(serialPort: SerialPort): EventBased
}

trait StreamBased extends RcOp:
  def close(): Unit

  def perform(request: String): RcResponse

  def perform(requests: Seq[String]): Seq[RcResponse]

trait EventBased extends RcOp:

  def addDataListener(serialPortDataListener: SerialPortDataListener): Unit

  def send(string: String): Unit
  def close(): Unit