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

import com.typesafe.config.ConfigFactory
import net.wa9nnn.rc210.data.field.FieldDefinitions
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.io.DatFile
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try, Using}

class JsonFileLoaderSpec extends WithTestConfiguration {
  private val url = getClass.getResource("/data/datastore.json")

  "load" should {
    "apply" in {
      val fieldDefinitions = new FieldDefinitions()

      val datFile = new DatFile(config)
      val memoryFileLoader: MemoryFileLoader = new MemoryFileLoader(fieldDefinitions, datFile)
      val seq = memoryFileLoader.load

      val dataStoreJson = new DataStoreJson(datFile)
      val dataStore = new DataStore(dataStoreJson)
      dataStore.load(seq)
      dataStore.all must haveLength(301)

      Using(url.openStream()) { inputStream =>
        val seq: Seq[FieldEntryJson] = Json.parse(inputStream).as[Seq[FieldEntryJson]]
        val dataTransferJson = new DataTransferJson(seq, Seq.empty)
        dataStore.fromJson(dataTransferJson)
      }
      dataStore.all must haveLength(301) //stl
    }
  }
}
