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

package net.wa9nnn.rc210.serial.comm

import com.fazecast.jSerialComm.SerialPort
import net.wa9nnn.rc210.serial.ComPort

import java.io.{PrintWriter, StringWriter}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try, Using}

/**
 * A guru test since it must be connected to an RC210
 */
object DataCollectorTest extends App {
  def listPorts: Seq[ComPort] = {
    val ports = SerialPort.getCommPorts
    ports.map(ComPort(_)).toList
  }

  def ft232Port: ComPort = {
    listPorts.find(_.friendlyName.contains("FT232")) match {
      case Some(value) =>
        value
      case None =>
        throw new IllegalStateException("Can't find FT232 port")
    }
  }

  val lines: Try[String] = Using.Manager { use =>
    val stringWriter = use(new StringWriter())
    val writer = use(new PrintWriter(stringWriter))

    val dcs: DataCollectorStuff = DataCollector(writer, ft232Port.descriptor)
    val starting: Progress = dcs.progressSource()
    println(s"StartingProgress: $starting")
    val finalProgress: Progress = Await.result[Progress](dcs.future, 600 seconds)
    writer.close()
    stringWriter.toString
  }

  lines match {
    case Failure(exception) =>
      exception.printStackTrace()
    case Success(lines) =>
      println(lines)
  }

}