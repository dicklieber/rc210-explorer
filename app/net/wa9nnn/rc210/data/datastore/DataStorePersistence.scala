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

package net.wa9nnn.rc210.data.datastore

import com.github.andyglow.config.*
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.NamedKey
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.util.Configs
import play.api.libs.json.{Format, Json}

import java.io.InputStream
import java.nio.file.{Files, Path}
import javax.inject.Inject
import scala.collection.immutable.Seq
import scala.util.{Try, Using}

/**
 * Parses JSON saved from [[DataStore]]
 */

class DataStorePersistence @Inject()( implicit config: Config) extends LazyLogging {
  def save(dataTransferJson: DataTransferJson): Unit = {
    Files.writeString(path,
      toJson(dataTransferJson))
  }

  def toJson(dataTransferJson: DataTransferJson): String = {
    Json.prettyPrint(
      Json.toJson(dataTransferJson)
    )
  }

  private val path: Path = Configs.path("vizRc210.dataStoreFile")

  def load(): Try[DataTransferJson] = {
    Using(Files.newInputStream(path)) { inputStream =>
      Json.parse(inputStream).as[DataTransferJson]
    }
  }

}

/**
 * Data transfer between [[DataStorePersistence]] and [[DataStore]].
 * Json-friendly data that is persisted or doewnloaded from the [[DataStore]].
 */
case class DataTransferJson(values: Seq[FieldEntryJson], namedKeys: Seq[NamedKey], who: Option[Who] = None) {
  def toPrettyJson: String = {
    Json.prettyPrint(Json.toJson(this))
  }
}

object DataTransferJson {
  implicit val fmtDataTransferJson: Format[DataTransferJson] = Json.format[DataTransferJson]
}


