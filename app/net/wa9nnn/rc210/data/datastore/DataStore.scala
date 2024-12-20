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

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210
import net.wa9nnn.rc210.*
import net.wa9nnn.rc210.ui.NamedKeyManager
import net.wa9nnn.rc210.util.Configs

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

/**
 * This is the in-memory source of all RC-210 and NamedKey data.
 */
@Singleton
class DataStore @Inject()(config: Config,
                          memoryFileLoader: MemoryFileLoader,
                          namedKeyManager: NamedKeyManager) extends DataStorePersistence with LazyLogging:
  private val path: Path = Configs.path("vizRc210.dataStoreFile")(using config)

  memoryFileLoader.load.foreach { fieldEntries =>
    loadEntries(fieldEntries) 
  }
  load()

  def update(candidates: Seq[UpdateCandidate]): Unit
    super.update(candidateAndNames)
    saveFile(path)

  def load(): Unit =
    loadFile(path)
    
    
    
//  private def save(session: RcSession): Unit =
//    val dto: DataTransferJson = toJson.copy(who = Some(session.user.who))
//    persistence.save(dto)

/*
  /**
   * Update values from datastore.json
   */
  def loadFromJson(): Unit =
    try
      persistence.load().foreach { dto =>
        dto.values.foreach { fieldEntryJson =>
          val fieldKey = fieldEntryJson.fieldKey
          keyFieldMap.get(fieldKey).foreach { fieldEntry =>
            val newFieldValue: FieldValue = fieldEntry.fieldDefinition.parse(fieldEntryJson.fieldValue)
            val newCandidate: Option[FieldValue] = fieldEntryJson.candidate.map(fieldEntry.fieldDefinition.parse)

            val updated = fieldEntry.copy(fieldValue = newFieldValue, candidate = newCandidate)
            keyFieldMap.put(fieldKey, updated)
          }
        }
      }

    catch
      case e: Exception =>
        logger.error("Loading", e)
*/
//
//  private def loadFromMemory(): Unit =
//    memoryFileLoader.load match
//      case Failure(exception) =>
//        logger.error("Loading DataStore from Download Memory image.", exception)
//      case Success(fieldEntries: Seq[FieldEntry]) =>
//        fieldEntries.foreach { fe =>
//          keyFieldMap.put(fe.fieldKey, fe)
//        }
//
//  def toJson: DataTransferJson =
//    DataTransferJson(values = keyFieldMap.values.map(FieldEntryJson(_)).toSeq)
//
//
