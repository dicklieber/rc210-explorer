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
import net.wa9nnn.rc210.serial.comm.{DownloadStatus, Progress}
import net.wa9nnn.rc210.util.CircularBuffer

import java.io.IOException
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.io.BufferedSource
import scala.util.Try
import scala.util.matching.Regex



object ERamCollector {
  val parser: Regex = """(\d+),(\d+)""".r

  def listPorts: List[ComPort] = {
    SerialPort.getCommPorts.map(ComPort(_)).toList
  }
}

//case class RC210Data(mainArray: Array[Int], extArray: Array[Int], progress: EramStatus)

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