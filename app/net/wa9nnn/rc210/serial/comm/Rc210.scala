package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, RowSource}
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse
import net.wa9nnn.rc210.serial.{ComPort, ComPortPersistence}

import java.io.IOException
import javax.inject.{Inject, Singleton}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


@Singleton
class Rc210 @Inject()(comPortPersistence: ComPortPersistence) {
  def sendOne(name: String, request: String): RcOperationResult = {
    sendBatch(name, request) match {
      case Failure(exception) =>
        throw exception
      case Success(bor: BatchOperationsResult) =>
        bor.results.head
    }
  }

  def sendBatch(name: String, requests: String*): Try[BatchOperationsResult] = {
    for {
      comPort <- comPortPersistence.currentComPort
    } yield {
      val rcOperation = new RcOperationImpl(comPort)
      try {
        rcOperation.sendBatch(name, requests: _*)
      } finally
        rcOperation.close()
    }
  }

  /**
   *
   * @return error or an [[RcOperation]] that can be used to [[RcOperation.sendBatch()]]
   */
  def start: Try[RcOperation] = {
    for {
      comPort <- comPortPersistence.currentComPort
    } yield {
      new RcOperationImpl(comPort)
    }

  }

  /**
   * Allow one or more operations with a serial port.
   * This should be used for all RC210 IO except for download which should use [[DataCollector]]
   *
   * @param comPort to use.
   */
  class RcOperationImpl(comPort: ComPort) extends RcOperation with SerialPortMessageListenerWithExceptions with AutoCloseable with LazyLogging {
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

    def sendOne(request: String): RcOperationResult = {
      currentTransaction match {
        case Some(currentTransaction) =>
          throw BusyException(currentTransaction)
        case None =>
          logger.trace("Perform: {}", request)
          val transaction = Transaction(request + "\r", serialPort)
          currentTransaction = Option(transaction)
          RcOperationResult(request, Try {
            val r: RcResponse = Await.result[RcResponse](transaction.future, 30 seconds)
            r
          })
      }
    }

    def sendBatch(name: String, requests: String*): BatchOperationsResult = {
      BatchOperationsResult(name, requests.map { request =>
        sendOne(request)
      })
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
}



object RcOperation extends LazyLogging {

  type RcResponse = Seq[String]

}


case class BusyException(currentTransaction: Transaction) extends Exception(currentTransaction.toString)

trait RcOperation {
  def sendOne(request: String): RcOperationResult

  def sendBatch(name: String, requests: String*): BatchOperationsResult

  def close(): Unit
}
