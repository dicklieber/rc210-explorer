package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, Table}
import configs.syntax._
import net.wa9nnn.rc210.serial.comm.RcOperation.RcResponse
import net.wa9nnn.rc210.serial.{ComPort, NoPortSelected, OpenedSerialPort, RcSerialPort}

import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Try, Using}


@Singleton
class Rc210 @Inject()(serialPortsSource: SerialPortsSource, config: Config) {
  def openSerialPort: OpenedSerialPort = maybeRcSerialPort.map(_.openPort()).get

  private val file: Path = config.get[Path]("vizRc210.serialPortsFile").value

  Files.createDirectories(file.getParent)
  var maybeRcSerialPort: Option[RcSerialPort] = None
  selectPort(Files.readString(file))


  def sendOne(request: String): RcOperationResult = {
    Using.resource(new RcOperationImpl()) { rcOperation =>
      rcOperation.sendOne(request)
    }
  }

  def sendBatch(name: String, requests: String*): BatchOperationsResult = {
    Using.resource(new RcOperationImpl()) { rcOperation =>
      rcOperation.sendBatch(name, requests: _*)
    }
  }

  /**
   *
   * @return an RcOperation
   */
  @throws[NoPortSelected]
  def start: RcOperation = {
    new RcOperationImpl()
  }

  /**
   * Allow one or more operations with a serial port.
   * This should be used for all RC210 IO except for download which should use [[DataCollector]]
   * Clients only know about [[RcOperation]].
   *
   */
  private[Rc210] class RcOperationImpl() extends RcOperation with AutoCloseable with LazyLogging {
    private val openedSerialPort: OpenedSerialPort = openSerialPort

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

  def selectPort(portDescriptor: String): Unit =
    serialPortsSource().find(_.descriptor == portDescriptor)
      .foreach { comPort =>

        Files.writeString(file, comPort.descriptor)
        maybeRcSerialPort = Option(RcSerialPort(SerialPort.getCommPort(comPort.descriptor)))
      }

  def table(): Table = {
    val currentComPOrt = maybeRcSerialPort.map(_.comPort)
    val rows: Seq[Row] = serialPortsSource().map { comPort: ComPort =>
      var row = comPort.toRow
      if (currentComPOrt.contains(comPort)) {
        row = row.withCssClass("selected")
      }
      row
    }

    val table = Table(Header("Serial Ports", "Descriptor", "Friendly Name"), rows)
    table
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

case class Rc210Version(version: String, comPort: ComPort)