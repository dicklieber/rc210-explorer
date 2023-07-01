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
import com.fazecast.jSerialComm.SerialPort
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.serial.{ComPort, CurrentSerialPort, RcSerialPort, RcSerialPortManager}
import org.apache.commons.io.FileUtils
import org.apache.commons.io.file.PathUtils
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.mockito.MockitoSugar

import java.nio.file.Files
import java.util
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.language.postfixOps
import scala.util.Success

/**
 * A guru test since it must be connected to an RC210
 */
class DataCollectorSpec extends WithTestConfiguration with MockitoSugar with BeforeAndAfterAll {
  def listPorts: Array[RcSerialPort] = {
    val ports = SerialPort.getCommPorts.map(RcSerialPort(_))
    ports
  }

  val ft232Port: RcSerialPort = {
    listPorts.find(_.comPort.friendlyName.contains("FT232")) match {
      case Some(value) =>
        value
      case None =>
        throw new IllegalStateException("Can't find FT232 port")
    }
  }

  val currentSerialPort = mock[CurrentSerialPort]
  private val progressApi: ProgressApi = mock[ProgressApi]
  implicit val testKit: ActorTestKit = ActorTestKit()
  //  implicit val system: ActorSystem[Nothing] = testKit.system
  private val dataStoreProbe = testKit.createTestProbe[DataStoreActor.Message]()
  when(currentSerialPort.currentPort) thenReturn(ft232Port)
  "DataCollector" should {
    "Produce File" in {
      val dcs: DataCollector = new DataCollector(config, currentSerialPort, dataStoreProbe.ref)
      dcs(progressApi)


      dataStoreProbe.expectMessage(120 seconds, DataStoreActor.Reload())

      Files.exists(dcs.memoryFile) should be(true)
      Files.exists(dcs.tempFile) should be(false)
      val strings: Seq[String] = Files.readAllLines(dcs.memoryFile).asScala.toIndexedSeq
      val last20 = strings.takeRight(20)
      last20.last should( be("390,0"))
    }
  }

  override def afterAll(): Unit =
    testKit.shutdownTestKit()


}
