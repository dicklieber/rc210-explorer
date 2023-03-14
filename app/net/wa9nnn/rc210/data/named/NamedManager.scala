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
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.key.Key
import net.wa9nnn.rc210.key.KeyFormats._
import net.wa9nnn.rc210.key.KeyKindEnum.KeyKind
import play.api.libs.json.Json

import java.io.IOException
import java.nio.file.{Files, Path, Paths}
import javax.inject.{Inject, Named, Singleton}

@Singleton
class NamedManager @Inject()(@Named("vizRc210.namedDataFile") namedFilePath: String) extends NamedSource with LazyLogging {

  private val namedFile: Path = Paths.get(namedFilePath)
  try {
    val created = Files.createDirectories(namedFile.getParent)
    logger.trace(s"Created parent: $created")
  } catch {
    case e: Throwable =>
      logger.error(e.getMessage)
  }

  private val map = new TrieMap[Key, String]

  def size: Int = map.size

  load()

  override def apply(key: Key): String = map.getOrElse(key, key.toString)

  def keysForKeyKind(keyKind: KeyKind): Seq[NamedKey] = {
    map
      .iterator
      .filter { case (key, _) => key.kind == keyKind }
      .map { case (key, value) =>
        NamedKey(key, value)
      }.toSeq
      .sorted
  }

  def update(data: Iterable[NamedKey]): Unit = {
    data.foreach { nk =>
      val name = nk.name
      val key = nk.key
      if (name.nonEmpty)
        map.put(key, name)
      else {
        map.remove(key)
      }
    }
    save()
  }

  def get(key: Key): Option[String] = map.get(key)


  private def save(): Unit = {
    val namedData = NamedData(map.map { case (key, str) =>
      NamedKey(key, str)
    }
      .toSeq
      .sorted)
    val sJson = Json.prettyPrint(Json.toJson(namedData))
    Files.writeString(namedFile, sJson)
    logger.trace("Saved named data to: {}", namedFile.toFile.toString)

  }

  private def load(): Unit = {

    try {
      logger.trace("Loading named data from: {}", namedFile.toFile.toString)
      if (Files.isReadable(namedFile) && Files.size(namedFile) > 0L) {
        val namedData = Json.parse(Files.readAllBytes(namedFile)).as[NamedData]
        namedData
          .data
          .foreach { nd =>
            map.put(nd.key, nd.name)
          }
      } else {
        logger.info(s"sEmpty named.json file: $namedFile")
      }
      logger.debug(s"Loaded $size names from $namedFile")
    } catch {
      case e: IOException =>
        logger.error(e.getMessage)
    }

  }
}

object NamedSource


case class NamedKey(key: Key, name: String) extends Ordered[NamedKey] {
  override def compare(that: NamedKey): Int = key compareTo that.key
}

case class NamedData(data: Seq[NamedKey])

trait NamedSource {
  def apply(key: Key): String
}