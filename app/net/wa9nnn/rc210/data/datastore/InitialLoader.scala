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

import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.io.DatFile
import play.api.libs.json.Json

import javax.inject.{Inject, Singleton}
import scala.util.Using


/**
 * Startup
 *
 * @param memoryFileLoader hwo to get memory image.
 * @param dataStore        where to put stuff
 * @param datFile          wjefre files are located based on [[com.typesafe.config.Config]].
 */
@Singleton
class InitialLoader @Inject()(memoryFileLoader: MemoryFileLoader, dataStore: DataStore, datFile: DatFile) {
  private val seq: Seq[FieldEntry] = memoryFileLoader.load(datFile.memoryFile)
  dataStore.load(seq)

  Using(datFile.dataStoreFile.openStream()) { inoutStream =>
    val seq: Seq[FieldEntryJson] = Json.parse(inoutStream).as[Seq[FieldEntryJson]]
    JsonFileLoader(seq, dataStore)
  }
}