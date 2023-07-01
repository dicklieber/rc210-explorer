package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.{SerialPort, SerialPortEvent, SerialPortMessageListenerWithExceptions}
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse
import net.wa9nnn.rc210.serial.{CurrentSerialPort, NoPortSelected, OpenedSerialPort, RcSerialPortManager}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Try, Using}


@Singleton
class Rc210 @Inject()(currentSerialPort: CurrentSerialPort) {
  def sendOne(request: String): RcOperationResult = {
    Using.resource(new RcOperationImpl(currentSerialPort.currentPort.openPort())) { rcOperation =>
      rcOperation.sendOne(request)
    }
  }

  def sendBatch(name: String, requests: String*): BatchOperationsResult = {
    Using.resource(new RcOperationImpl(currentSerialPort.currentPort.openPort())) { rcOperation =>
      rcOperation.sendBatch(name, requests:_*)
    }
  }

  /**
   *
   * @return an RcOperation
   */
  @throws[NoPortSelected]
  def start: RcOperation = {
    new RcOperationImpl(currentSerialPort.currentPort.openPort())
  }

  /**
   * Allow one or more operations with a serial port.
   * This should be used for all RC210 IO except for download which should use [[DataCollector]]
   *
   * @param openedSerialPort to use.
   */
  private[Rc210] class RcOperationImpl(openedSerialPort: OpenedSerialPort) extends RcOperation  with AutoCloseable with LazyLogging {
    assert(openedSerialPort.rcSerialPort.serialPort.isOpen, "Serial port not open!")

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
          val transaction = Transaction(request + "\r", openedSerialPort)
          currentTransaction = Option(transaction)
          val rcOperationResult: RcOperationResult = RcOperationResult(request, Try {
            Await.result[RcResponse](transaction.future, 2 seconds)
          })
          currentTransaction = None
          rcOperationResult
      }
    }

    def sendBatch(name: String, requests: String*): BatchOperationsResult = {
      BatchOperationsResult(name, requests.map(sendOne))
    }


    def close(): Unit = {
      try {
        openedSerialPort.close()
      } catch {
        case e: Exception =>
          logger.error(s"$openedSerialPort", e)
      }
    }


  }
}


object RcOperation extends LazyLogging {

  type RcResponse = Seq[String]

}


case class BusyException(currentTransaction: Transaction) extends Exception(s"Busy: $currentTransaction")

trait RcOperation {
  def sendBatch(name: String, requests: String*): BatchOperationsResult

  def sendOne(request: String): RcOperationResult

  def close(): Unit
}
