/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.Stamped
import net.wa9nnn.rc210.util.{CircularBuffer, Progress}

import java.io.IOException
import java.time.{Duration, Instant}
import java.util.concurrent.Executors
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.io.BufferedSource
import scala.util.Try
import scala.util.matching.Regex

/**
 * Collects ERAM dat from an RC-210 via the serial port.
 *
 * @param descriptor       for [[SerialPort]].open
 * @param progress         called periodically to update progress bar.
 * @param executionContext where this wil run.
 */
class ERamCollector(descriptor: String, progress: Progress => Unit, mod: Int = 357)
  extends Runnable with LazyLogging {

  private val promise = Promise[RC210Data]()
  private val mainBuilder: mutable.Builder[Int, Seq[Int]] = Seq.newBuilder[Int]
  private val extBuilder: mutable.Builder[Int, Seq[Int]] = Seq.newBuilder[Int]
  private val serialPort: SerialPort = SerialPort.getCommPort(descriptor)
  private val recentLines = new CircularBuffer[String](50)
  private var builder = mainBuilder

  def start()(implicit executionContext: ExecutionContext): Future[RC210Data] = {
    executionContext.execute(this)
    promise.future
  }

  private val allLines: mutable.Builder[String, Seq[String]] = Seq.newBuilder[String]

  override def run(): Unit = {

    implicit val start = Instant.now()
    //    serialPort.setBaudRate(57600)
    val reader: BufferedSource = try {
      serialPort.setBaudRate(19200)
      val timeoutMs = 1000 * 60 * 2
      serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, timeoutMs, 0)

      val opened: Boolean = serialPort.openPort()
      if (!opened) {
        throw new IOException(s"Did not open $serialPort")
      }
      new BufferedSource(serialPort.getInputStream)
    } catch {
      case e: Exception =>
        promise.tryFailure(e)
        return // out of the run
    }

    wakeup()

    write("1SendEram\r\n") // tell RC-210 to send Erqam contents.

    try {
      reader.getLines().foreach { line =>
        logger.debug(s"line: $line")
        recentLines.add(line)
        logger.whenTraceEnabled {
          allLines += line
        }
        line match {
          case "Complete" =>
            logger.debug("Complete")
            promise.complete {
              Try {
                serialPort.closePort()
                RC210Data(mainBuilder.result(), extBuilder.result(), start)
              }
            }

          case "EEPROM Done" =>
            // done with main part switch to saving in external builder.
            logger.debug("EEPROM Done switch to ext EEPROM.")
            builder = extBuilder

          case message: String =>
            try {
              val ERamCollector.parser(sIndex, value) = message
              val index: Int = sIndex.toInt
              if (index % mod == 0) {
                progress(Progress(index))
              }
              builder += value.toInt
            } catch {
              case e: Exception =>
                logger.error(s"processline line: $line")
            }
            write("\rOK\r\n")
          case x =>
            logger.error(s"Unexpected Message: $x")
        }
      }
    } catch {
      case e: Exception =>
        logger.error("Line Processing", e)
        logger.error(recentLines.items.mkString("\n"))
    }
    allLines.result().foreach {
      println
    }

    def wakeup(): Unit = {
      for (_ <- 0 to 3) {
        write("\r")
        Thread.sleep(100)
      }
    }

    def write(s: String): Unit = {
      val bytes = s.getBytes
      serialPort.writeBytes(bytes, bytes.length)
    }
  }
}

object ERamCollector {
  val parser: Regex = """(\d+),(\d+)""".r

  def listPorts: List[ComPort] = {
    SerialPort.getCommPorts.map(ComPort(_)).toList
  }

}

case class RC210Data(memory: Seq[Int], extMemory: Seq[Int], override val stamp: Instant) extends Stamped

object ErmamTest extends App {
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))


  val maybePort: Option[ComPort] = ERamCollector.listPorts.find(_.friendlyName.contains("FT232"))
  maybePort.map { comPort =>
    val collector = new ERamCollector(comPort.descriptor, progress =>
      println(progress)
      , 500)

    collector.start().foreach { r: RC210Data =>
      println(r)
    }
  }.orElse(throw new IllegalStateException(s"Can't find a FT232 serial port!"))
}