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

package net.wa9nnn.rc210.data.named

import com.typesafe.config.{Config, ConfigFactory, ConfigValue}
import net.wa9nnn.rc210.key.{MacroKey, PortKey}
import org.specs2.mutable.Specification
import org.specs2.mock._

import java.nio.file.{Files, Path}

class NamedManagerSpec extends Specification with Mockito {

  private val configMock: Config = mock[Config]
  private val path: Path = Files.createTempFile("vizrc210", "named.json")
   val bool: Boolean = Files.exists(path)
  private val filePath: String = path.toString
  val namedManager = new NamedManager(filePath)
  val key = PortKey(2)

  "NamedManager" should {
//    "save" in {
//      namedManager.apply(MacroKey(42), "named42named")
//      println(path)
//
//      ok
//    }
    "round trip" >> {
      namedManager.size must beEqualTo (0)
      val keyValue = "Groucho"
      namedManager.update(Seq(NamedKey(key, keyValue)))
      namedManager(key) must beEqualTo (keyValue)
      namedManager(key) must beEqualTo (keyValue)
      val newInstance = new NamedManager(filePath)
      newInstance.size must beEqualTo (1)
      newInstance(key) must beEqualTo (keyValue)
    }
  }
}
