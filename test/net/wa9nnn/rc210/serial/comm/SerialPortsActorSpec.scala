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
import net.wa9nnn.RAsyncSpec
import net.wa9nnn.rc210.serial.ComPort
import net.wa9nnn.rc210.serial.comm.SerialPortsActor.{CurrentPort, SelectPort, SerialPorts}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Json
import play.libs.Scala.None

import java.nio.file.{Files, Path}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.concurrent.impl.Promise
import scala.language.postfixOps

class SerialPortsActorSpec() extends RAsyncSpec {
  private val testKit = ActorTestKit()
  implicit val sys = testKit.system
  implicit val context: ExecutionContextExecutor = sys.executionContext
  implicit val sch: Scheduler = testKit.scheduler
  implicit val timeout: Timeout = 30 seconds

  val serialPortsSource: SerialPortsSource = mock[SerialPortsSource]
  private val comPort1: ComPort = ComPort("com1", "1")
  private val comPort2: ComPort = ComPort("com2", "2")
  when(serialPortsSource.apply()).thenReturn(Seq(comPort1, comPort2))

  private val file: Path = Files.createTempFile("currentPort", ".txt")
  private val sFile: String = file.toFile.toString

  val portActor: ActorRef[SerialPortsActor.Message] = testKit.spawn(SerialPortsActor(sFile, serialPortsSource, testKit.system.executionContext))

  "SerialPortsActor" when {
    "List Ports " in {
      portActor.ref.ask(SerialPorts) map { comports =>
        comports.head.descriptor should equal("com1")
        comports.head.friendlyName should equal("1")
        assert(comports.nonEmpty)
      }
    }
    "SendReceive" in {
      val future: Future[Seq[String]] = portActor.ref.ask(SerialPortsActor.SendReceive("1GetVersion", _))


      future.map { strings: Seq[String] =>
        println(strings)
        strings should have length (2)
      }
    }
    "select and get current port" in {
      portActor.ref.ask(CurrentPort) map { maybeCurrentPort =>
        maybeCurrentPort shouldBe None
      }
      portActor.ref ! SelectPort(comPort2)

      portActor.ref.ask(CurrentPort) map { maybeCurrentPort =>
        maybeCurrentPort shouldBe Some(comPort2)
        val fileContents = Files.readString(file)
        val backAgain = Json.parse(fileContents).as[ComPort]
        backAgain should equal(comPort2)
      }

    }
  }
}

