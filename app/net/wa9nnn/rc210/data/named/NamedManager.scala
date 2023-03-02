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

import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import com.typesafe.config.Config
import net.wa9nnn.rc210.key.Key
import net.wa9nnn.rc210.key.KeyFormats._
import play.api.libs.json.Json

import java.nio.file.{Files, Paths}
import javax.inject.{Inject, Singleton}

@Singleton
class NamedManager @Inject()(config: Config) extends NamedSource{
  private val path = Paths.get(config.getString("vizRc210.dataDir"))
  private val namedFile = path.resolve("named.json")
  Files.createDirectories(path)

  private val map = new TrieMap[Key, String]

  override def apply(key: Key): String = map.getOrElse(key, key.toString)

  def apply(key: Key, str: String): Unit = {
    if (str.nonEmpty)
      map.put(key, str)
    else {
      map.remove(key)
      save()
    }
  }


  private def save(): Unit = {
    val namedData = NamedData(map.map { case (key, str) =>
      NamedKey(key, str)
    }.toSeq)
    val sJson = Json.prettyPrint(Json.toJson(namedData))
    Files.writeString(namedFile, sJson)
  }
}

case class NamedKey(key: Key, name: String)

case class NamedData(data: Seq[NamedKey])

trait NamedSource {
  def apply(key: Key):String
}