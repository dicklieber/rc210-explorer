package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try, Using}

object Version extends LazyLogging:
  def apply(serialPort: SerialPort): Try[String] =

    val tried: Try[String] = Using(RealStreamBased(serialPort)){ rcStreamBased =>
      val response: RcResponse = rcStreamBased.perform("\r\r1GetVersion")
      val versionLine = response.lines.head
      
      val left = versionLine.head
      val right: String = versionLine.drop(1)
      val r = s"$left.$right"
      r
    }
    tried match
      case Failure(exception) => 
        logger.error("Version", exception)
        tried
      case Success(value) => 
        tried


