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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.field.{FieldDefinition, FieldEntry}
import net.wa9nnn.rc210.io.DatFile
import play.api.libs.json.Json

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try, Using}


@Singleton
class JsonFileLoader @Inject()(dataStore: DataStore, datFile: DatFile, memoryFileLoader: MemoryFileLoader) extends LazyLogging {

  load()

  def load(): Unit = {

    JsonFileLoader(datFile.dataStoreFile.toUri.toURL) match {
      case Failure(exception) =>
        logger.info(s"No Json file. (${exception.getMessage})")
      case Success(fields: Seq[FieldEntryJson]) =>
        loadFields(fields)
    }

    def loadFields(fields: Seq[FieldEntryJson]): Unit = {

      val r: Seq[FieldEntry] = for {
        json <- fields
        fieldKey = json.fieldKey
        fieldEntry <- dataStore(fieldKey)
      } yield {
        val fieldDefinition: FieldDefinition = fieldEntry.fieldDefinition
        FieldEntry(fieldDefinition = fieldDefinition,
          fieldKey = fieldKey,
          fieldValue = fieldDefinition.parse(json.fieldValue),
          candidate = json.candidate.map { o =>
            fieldDefinition.parse(o)
          })
      }

      dataStore.update(r)
    }
  }
}

/**
 * Parses JSON saved from [[DataStore]]
 */
object JsonFileLoader {
  /**
   * Loads a JSON file parwses to [[FieldEntryJson]]s
   *
   * @param url
   * @return
   */
  def apply(url: URL): Try[Seq[FieldEntryJson]] = {
    Using(url.openStream()) { inputStream =>
      Json.parse(inputStream).as[Seq[FieldEntryJson]]
    }
  }

}