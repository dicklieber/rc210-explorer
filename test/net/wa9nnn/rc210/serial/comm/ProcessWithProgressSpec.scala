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
import akka.stream.{ActorMaterializer, Materializer}
import net.wa9nnn.RcSpec
import org.scalatest.funsuite.AnyFunSuiteLike
import play.api.mvc.WebSocket

class ProcessWithProgressSpec extends RcSpec {
  implicit val testKit: ActorTestKit = ActorTestKit()
  implicit val materializer = Materializer(testKit.system)

 "ProcessWithProgress" should {
   val websocket: WebSocket = ProcessWithProgress(10, None){ progApi =>

     progApi.doOne("Hello one")
     progApi.finish("Kinder das ist Alles")

   }
 }

}
