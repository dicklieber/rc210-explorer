package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Try, Using}

object Version extends LazyLogging:
  def apply(serialPort: SerialPort): Try[String] =

    Using(RcStreamBased(serialPort)){rcStreamBased =>
      val response: RcResponse = rcStreamBased.perform("\r\r1GetVersion\r")
      val lines = response.lines
      logger.whenWarnEnabled {
        if (lines.nonEmpty)
          logger.debug("Getting version:")

        lines.foreach(line =>
          logger.debug(s"\t:$line")
        )
      }
      val versionLine: String = lines.head
      val left = versionLine.head
      val right: String = versionLine.drop(1)
      val r = s"$left.$right"
      r
    }


