package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, RowSource}
import net.wa9nnn.rc210.serial.ComPort
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse

import java.io.IOException
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try, Using}


/**
 * Allow one or more operations with a serial port.
 * This should be used for all RC210 IO except for download which should use [[DataCollectorActor]]
 *
 * @param comPort to use.
 */
case class RcOperation(comPort: ComPort) extends SerialPortMessageListenerWithExceptions with AutoCloseable with LazyLogging {
  val serialPort: SerialPort = SerialPort.getCommPort(comPort.descriptor)

  serialPort.setBaudRate(19200)
  private val opened: Boolean = serialPort.openPort()
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

  def perform(request: String): RcOperationResult = {
    currentTransaction match {
      case Some(currentTransaction) =>
        throw BusyException(currentTransaction)
      case None =>
        logger.trace("Perform: {}", request)
        val transaction = Transaction(request + "\r", serialPort)
        currentTransaction = Option(transaction)
        RcOperationResult(request, Try {
          Await.result[RcResponse](transaction.future, 30 seconds)
        })
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

  /**
   * Invoked by jSerialComm on it's own thread.
   *
   * @param event from [[SerialPort]].
   */
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

case class RcOperationResult(request: String, triedResponse: Try[RcResponse]) extends RowSource {
  def isSuccess: Boolean = triedResponse.isSuccess

  def isFailure: Boolean = triedResponse.isFailure

  def head: String = triedResponse match {
    case Failure(exception) =>
      exception.getMessage
    case Success(rcResponse: RcResponse) =>
      rcResponse.head
  }

  private def fixUp(in: String): String =
    in.replace("\r", "\\r")
      .replace("\n", "\\n")

  private def flatten(rcResponse: RcResponse): String = rcResponse.map(fixUp).mkString(" ")

  override def toRow(): Row = {
    triedResponse match {
      case Failure(exception) =>
        Row.ofAny(request, exception.getMessage)
      case Success(rcResponse: RcResponse) =>
        Row.ofAny(request, flatten(rcResponse))
    }
  }
   def toRow(rowHeader:Any, rowspan:Int): Row = {
    triedResponse match {
      case Failure(exception) =>
        Row.ofAny(request, exception.getMessage)
      case Success(rcResponse: RcResponse) =>
        Row(Cell(rowHeader)
          .withRowSpan(rowspan),
          request,
          flatten(rcResponse))
    }
  }

  override def toString: String = {
    triedResponse match {
      case Failure(exception) =>
        s"$request => ${exception.getMessage}"
      case Success(rcResponse: RcResponse) =>
        s"$request => ${flatten(rcResponse)}"
    }
  }
}

object RcOperationResult {
  def header(count: Int): Header = Header(s"RC Operation Results ($count)", "Field", "Command", "Response")
}

object RcOperation extends LazyLogging {
  /**
   * Simple connect->send->receive->close
   *
   * @param request to be sent to RC210
   * @param comPort of the RC210
   * @return response failure exception or Seq[String].
   */
  def apply(request: String, comPort: ComPort): RcOperationResult = Using(RcOperation(comPort)) { rr =>
    rr.perform(request)
  }.get

  type RcResponse = Seq[String]

}


case class BusyException(currentTransaction: Transaction) extends Exception(currentTransaction.toString)


