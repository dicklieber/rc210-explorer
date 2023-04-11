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

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ERamCollectorSpec(implicit ee: ExecutionEnv) extends Specification {
  "ERamCollectorSpec" should {
    "start" in {

      val maybePort = ERamCollector.listPorts.find(_.friendlyName.contains("FT232")).get
      val collector = new ERamCollector(maybePort.descriptor)

      val future: Future[RC210Data] = collector.start()
      val result: RC210Data = Await.result[RC210Data](future, 2 minutes)
      ok
    }
  }
}
