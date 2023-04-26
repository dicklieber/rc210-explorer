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
import net.wa9nnn.rc210.data.field.FieldEntry

import javax.inject.{Inject, Singleton}


/**
 * Startup
 *
 * @param memoryFileLoader hwo to get memory image.
 * @param dataStore        where to put stuff
 * @param datFile          wjefre files are located based on [[com.typesafe.config.Config]].
 */
@Singleton
class InitialLoader @Inject()(memoryFileLoader: MemoryFileLoader, dataStoreJson: DataStoreJson, dataStore: DataStore) extends LazyLogging{

   try {
     val seq: Seq[FieldEntry] = memoryFileLoader.load
     dataStore.load(seq)

     dataStoreJson.load(dataStore)
   } catch {
     case e:Exception =>
       logger.error("Initial load", e)
   }
}
