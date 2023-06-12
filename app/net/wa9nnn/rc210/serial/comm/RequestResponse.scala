package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.ComPort

import java.io.IOException
import java.time.Instant
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.language.postfixOps


/**
 * Allow one or more operations with a serial port.
 *
 * @param comPort from [[net.wa9nnn.rc210.serial.comm.Rc210Serial#listPorts()]].
 */
class RequestResponse(comPort: ComPort) extends SerialPortMessageListenerWithExceptions with LazyLogging {
  val serialPort: SerialPort = SerialPort.getCommPort(comPort.descriptor)

  serialPort.setBaudRate(19200)
  val opened: Boolean = serialPort.openPort()
  if (!opened) {
    throw new IOException(s"Did not open $comPort")
  }
  serialPort.addDataListener(this)

  private var currentTransaction: Option[Transaction] = None

  /**
   *
   * @param request will have cr appended
   * @return
   */

  def perform(request: String): Future[Seq[String]] = {
    currentTransaction match {
      case Some(currentTransaction) =>
        throw BusyException(currentTransaction)
      case None =>
        logger.trace("Perform: {}", request)
        val transaction = Transaction(request + "\r", serialPort)
        currentTransaction = Option(transaction)
        transaction.future
    }
  }

  def close(): Unit = {
    try {
      serialPort.closePort()
    } catch {
      case e: Exception =>
        logger.error(s"Closing: $comPort ${e.getMessage}")
    }
  }

  override def catchException(e: Exception): Unit = {
    logger.error(s"$comPort", e)
  }

  override def getMessageDelimiter: Array[Byte] = Array('\n')

  override def delimiterIndicatesEndOfMessage(): Boolean = true

  override def getListeningEvents: Int = LISTENING_EVENT_DATA_RECEIVED

  override def serialEvent(event: SerialPortEvent): Unit = {
    val receivedData: Array[Byte] = event.getReceivedData
    val response = new String(receivedData).trim
    currentTransaction match {
      case Some(transaction: Transaction) =>
        if (transaction.collect(response))
          currentTransaction = None
      case None =>
        logger.error(s"""received "$response" but no transaction is waiting, ignored!""")
    }
  }
}

case class BusyException(currentTransaction: Transaction) extends Exception(currentTransaction.toString)

