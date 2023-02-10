package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{Json, OFormat}

import java.io.{IOException, OutputStream}
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.io.BufferedSource
import scala.util.matching.Regex
import scala.util.{Try, Using}

case object RC210Download extends LazyLogging {
  val parser: Regex = """(\d+),(\d+)""".r

  def listPorts: List[ComPort] = {
    SerialPort.getCommPorts.map(ComPort(_)).toList
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

  def wakeup(outputStream: OutputStream): Unit = {
    for (_ <- 0 to 3) {
      outputStream.write('\r'.toInt)
      outputStream.flush()
      Thread.sleep(100)
    }

  }

}

case class ComPort(descriptor: String, friendlyName: String)

object ComPort {
  implicit val comPortFmt: OFormat[ComPort] = Json.format[ComPort]
  def apply(serialPort: SerialPort): ComPort = {
    new ComPort(serialPort.getSystemPortPath, serialPort.getDescriptivePortName)
  }
}
