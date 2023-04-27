package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{Json, OFormat}

import java.io._
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path, Paths}
import javax.inject.{Inject, Singleton}
import scala.annotation.tailrec
import scala.collection.mutable
import scala.io.BufferedSource
import scala.util.matching.Regex
import scala.util.{Failure, Try, Using}

@Singleton
class RC210IO @Inject()(config: Config) extends LazyLogging {
  val parser: Regex = """(\d+),(\d+)""".r

  def listPorts: List[ComPort] = {
    SerialPort.getCommPorts.map(ComPort(_)).toList
  }

  /**
   * temporary way to get port.
   * //todo persist user chosen port in future.
   *
   * @return
   */
  def ft232Port: ComPort = {
    listPorts.find(_.friendlyName.contains("FT232")) match {
      case Some(value) =>
        value
      case None =>
        throw new IllegalStateException("Can't find FT232 port")
    }
  }

  def download(descriptor: String): Try[Array[Int]] = {
    listPorts.find(_.descriptor == descriptor) match {
      case Some(comPort) =>
        download(comPort)
      case None =>
        throw new IllegalArgumentException(s"Can't find serial port: $descriptor!")
    }
  }

  def download(comPort: ComPort): Try[Array[Int]] = {
    val linesFile: Option[Path] = Option.when(logger.underlying.isTraceEnabled()) {
      val logsDir = Paths.get("logs")
      Files.createDirectories(logsDir)
      val path = Files.createTempFile(logsDir, "RC210Lines", ".txt")
      logger.trace("Saving lines to file: {}", path)
      path
    }

    val serialPort: SerialPort = SerialPort.getCommPort(comPort.descriptor)
    //    serialPort.setBaudRate(57600)
    serialPort.setBaudRate(19200)
    val opened: Boolean = serialPort.openPort()
    if (!opened) {
      throw new IOException(s"Did not open $comPort")
    }

    serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0)
    val outputStream: OutputStream = serialPort.getOutputStream

    val result: mutable.ArrayBuilder[Int] = Array.newBuilder[Int]

    wakeup(outputStream)
    outputStream.write("1SendEram\r\n".getBytes)

    Using(new BufferedSource(serialPort.getInputStream)) { source: BufferedSource =>
      source
        .getLines()
        .takeWhile(_.head != 'C')
        .takeWhile(_ != "EEPROM Done")
        .filterNot(_.startsWith("-"))
        //        .take(200)//todo remove
        .foreach { line =>
          linesFile.foreach(Files.writeString(_, line + "\n", CREATE, WRITE, APPEND))
          try {
            //            logger.trace("line: {}", line)
            if (line.nonEmpty) {
              val parser(sIndex, value) = line
              val index = sIndex.toInt
              logger.whenDebugEnabled {
                if (index % 250 == 0) {
                  logger.debug(sIndex)
                }
              }
              result += value.toInt
            }
            outputStream.write("\rOK\r\n".getBytes)

          } catch {
            case e: Exception =>
              logger.error(s"Processing line: $line", e)
          }
        }
      result.result()
    }
  }

  def start(comPort: ComPort = ft232Port): SerialPortOperation = {
    val comPort: ComPort = ft232Port
    new SerialPortOperation(comPort)
  }

  def wakeup(outputStream: OutputStream): Unit = {
    for (_ <- 0 to 3) {
      outputStream.write('\r'.toInt)
      outputStream.flush()
      Thread.sleep(100)
    }
  }
}




case class ComPort(descriptor: String = "com1", friendlyName: String = "com1")

object ComPort {
  implicit val comPortFmt: OFormat[ComPort] = Json.format[ComPort]

  def apply(serialPort: SerialPort): ComPort = {
    new ComPort(serialPort.getSystemPortPath, serialPort.getDescriptivePortName)
  }
}