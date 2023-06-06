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
import com.wa9nnn.util.tableui.{Row, Table}
import net.wa9nnn.rc210.util.{CircularBuffer, EramStatus, Progress}

import java.io.IOException
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.io.BufferedSource
import scala.util.Try
import scala.util.matching.Regex

/**
 * Collects ERAM dat from an RC-210 via the serial port.
 *
 * @param descriptor       for [[SerialPort]].open
 */
class ERamCollector(descriptor: String) extends Runnable with LazyLogging  {

  private val promise = Promise[RC210Data]()
  private val serialPort: SerialPort = SerialPort.getCommPort(descriptor)
  private val recentLines = new CircularBuffer[String](50)
  private var status = new EramStatus(ComPort(serialPort))

  // Note these are [[Array]]s rather than [[List]]s  as they will be accessed, a lot by the nth element,
  // see https://stackoverflow.com/questions/2712877/difference-between-array-and-list-in-scala
  private val mainBuilder: mutable.ArrayBuilder[Int] = Array.newBuilder[Int]
  private val extBuilder: mutable.ArrayBuilder[Int] = Array.newBuilder[Int]
  private var builder = mainBuilder // will get switch between main and ext eram.
  private val count = new AtomicInteger()


  def progress: Progress = status.progress
  def resultStatus:EramStatus = status

  /**
   *
   * @param executionContext where this will run.
   * @return
   */
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

    reader.getLines().foreach { line =>
      try {
        logger.debug(s"line: $line")
        logger.whenInfoEnabled {
          recentLines.add(line)
        }
        logger.whenTraceEnabled {
          allLines += line
        }
        line match {
          case "Complete" =>
            logger.debug("Complete")
            promise.complete {
              Try {
                status.finish()
                reader.close()
                RC210Data(mainBuilder.result(), extBuilder.result(), status)
              }
            }
          case "Timeout" =>
            logger.info("Timeout ignoring.")

          case "EEPROM Done" =>
            // done with main part switch to saving in external builder.
            logger.debug("EEPROM Done switch to ext EEPROM.")
            builder = extBuilder

          case message: String =>
            try {
              val ERamCollector.parser(sIndex, value) = message
              //            val index: Int = sIndex.toInt
              status.update(count.incrementAndGet())
              builder += value.toInt
              write("\rOK\r\n")
            } catch {
              case e:MatchError =>
                logger.error(s"MatchError on $message")
            }
          case x =>
            logger.error(s"Unexpected Message: $x")
        }

      } catch {
        case e: Exception =>
          logger.error(s"""Line Processing line: "$line" """, e)
        //        logger.error(recentLines.items.mkString("\n"))
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
    serialPort.closePort()
  }
}

object ERamCollector {
  val parser: Regex = """(\d+),(\d+)""".r

  def listPorts: List[ComPort] = {
    SerialPort.getCommPorts.map(ComPort(_)).toList
  }
}

case class RC210Data(mainArray: Array[Int], extArray: Array[Int], progress: EramStatus)

object ErmamTest extends App {
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))


  val maybePort: Option[ComPort] = ERamCollector.listPorts.find(_.friendlyName.contains("FT232"))
  maybePort.map { comPort =>
    val collector = new ERamCollector(comPort.descriptor)

    collector.start().foreach { r: RC210Data =>
      println(r)
    }
  }.orElse(throw new IllegalStateException(s"Can't find a FT232 serial port!"))
}