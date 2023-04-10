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

import java.io.IOException
import java.time.Instant
import java.util.concurrent.Executors
import scala.collection.mutable
import scala.concurrent.duration.Duration
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
class ERamCollector(descriptor: String, mod: Int = 357)(progress: Progress => Unit)
  extends Runnable with LazyLogging {

  private val promise = Promise[Seq[Int]]()
  private val builder: mutable.Builder[Int, Seq[Int]] = Seq.newBuilder[Int]
  private val serialPort: SerialPort = SerialPort.getCommPort(descriptor)

  def start()(implicit executionContext: ExecutionContext): Future[Seq[Int]] = {
    executionContext.execute(this)
    promise.future
  }

  override def run(): Unit = {


    //    serialPort.setBaudRate(57600)
    val reader: BufferedSource = try {
      serialPort.setBaudRate(19200)
      val timeoutMs = 1000 * 60
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

    write("1SendEram\r\n")

    reader.getLines.foreach { line =>
      logger.debug(s"line: $line")
      line match {
        case "EEPROM Done" =>
          logger.debug("EEPROM Done")
          promise.complete {
            Try {
              builder.result()
            }
          }
        case message: String =>
          logger.debug(message)
          val ERamCollector.parser(sIndex, value) = message
          val index: Int = sIndex.toInt
          if (index % mod == 0) {
            progress(Progress(index))
          }
          builder += value.toInt
          write("\rOK\r\n")
          logger.debug("Send OK")
        case x =>
          logger.error(s"Unexpected Message: $x")
      }
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

object ErmamTest extends App{
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))


  val maybePort = ERamCollector.listPorts.find(_.friendlyName.contains("FT232")).get
  val collector = new ERamCollector(maybePort.descriptor)(progress =>
    println(progress)
  )

  val start = Instant.now()
  collector.start().foreach{r: Seq[Int] =>
    val dur = java.time.Duration.between(start, Instant.now())
    println(s"Collected ${r.length} ints in: $dur}")
  }
}