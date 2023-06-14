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

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import net.wa9nnn.RcSpec
import net.wa9nnn.rc210.serial.ComPort
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar.mock

import java.nio.file.{Files, Path}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

class SerialPortsActorSpec() extends RcSpec {
  private val testKit = ActorTestKit()
  implicit val sys = testKit.system
  implicit val context: ExecutionContextExecutor = sys.executionContext
  implicit val sch: Scheduler = testKit.scheduler
  implicit val timeout: Timeout = 30 seconds

  val serialPortsSource: SerialPortsSource = mock[SerialPortsSource]
  when(serialPortsSource.apply()).thenReturn(Seq(ComPort("com1", "1"), ComPort("com2", "2")))

  private val file: Path = Files.createTempFile("currentPort", ".txt")
  private val sFile: String = file.toFile.toString

  val portActor: ActorRef[SerialPortsActor.DataCollectorMessage] = testKit.spawn(SerialPortsActor(sFile, serialPortsSource))

  "SerialPortsActor" when {
    "List Ports " in {
      val future: Future[Seq[ComPort]] = portActor.ref.ask(ref => SerialPortsActor.SerialPorts(ref))
      future map { comports => assert(comports.nonEmpty) }
    }
/*
    "Current Port " should {
      "initial have none" in {
        val future: Future[Option[ComPort]] = portActor.ref.ask(ref => SerialPortsActor.CurrentPort(ref))
        future map { current => assert(current.isEmpty) }
      }
      "one selected" in {
        val comPort = ComPort("111", "1f")
        portActor.ref ! SerialPortsActor.SelectPort(comPort)
        val futureWithSelected = portActor.ref.ask(ref => SerialPortsActor.CurrentPort(ref))
        futureWithSelected map { current => assert(current.get.descriptor == "111") }
      }
      val future: Future[Seq[ComPort]] = portActor.ref.ask(ref => SerialPortsActor.SerialPorts(ref))
      future map { comports => assert(comports.nonEmpty) }
    }
*/
  }
}

