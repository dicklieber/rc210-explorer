package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.scalalogging.LazyLogging

import java.io.{BufferedReader, InputStreamReader, OutputStream}

object ReadMemPlayPen extends App with LazyLogging {

  private val ports: Array[SerialPort] = SerialPort.getCommPorts
  private val maybePort: Option[SerialPort] = ports.find(_.getDescriptivePortName.contains("FT232"))




  val comPort: SerialPort = maybePort.get
  logger.info(comPort.toString)

  import com.fazecast.jSerialComm.SerialPort

  comPort.setBaudRate(19200)
  comPort.openPort
  comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0)

  private val reader = new InputStreamReader(comPort.getInputStream)
  val br = new BufferedReader(reader)


  private val outputStream: OutputStream = comPort.getOutputStream

  for (_ <- 0 to 3) {
    outputStream.write('\r'.toInt)
    outputStream.flush()
    Thread.sleep(100)
  }

  outputStream.write("1SendEram\r\n".getBytes)

  try {
    while (true) {
      val line = br.readLine()
      logger.info(line)

      if (line.contains("Complete"))
        throw Complete()
      outputStream.write("\rOK\r\n".getBytes)
    }
  } catch {
    case _: Complete =>
      logger.info("Kinder das ist alles!")
  }

}

case class Complete() extends Exception