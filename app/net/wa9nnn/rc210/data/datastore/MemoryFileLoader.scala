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
import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.data.datastore.MemoryFileLoader.notInitialized
import net.wa9nnn.rc210.data.field.{FieldDefinitions, FieldEntry}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.NamedKeyManager
import net.wa9nnn.rc210.util.Configs.path

import java.net.URL
import java.nio.file.Path
import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}

/**
 * Holds [[Memory]]
 *
 * @param fieldDefinitions
 * @param namedKeyManager we don't need this directly here but need have it loaded
 */
@Singleton
class MemoryFileLoader @Inject()( fieldDefinitions: FieldDefinitions)(implicit config: Config) extends LazyLogging {
  private val memoryFilePath: Path = path("vizRc210.memoryFile")
  private val memoryFile: URL = memoryFilePath.toUri.toURL

  private var tryMemory: Try[Memory] = Failure(notInitialized)

  def memory: Try[Memory] = tryMemory

  /**
   * Get saved Memory (Raw RC-210 download data.)
   *
   * @return [[Memory]] or the reason why.
   */
  private def loadMemory(): Unit = {
    tryMemory = Memory.load(memoryFile)
  }

  def load: Try[Seq[FieldEntry]] = {
    loadMemory()
    val r: Try[Seq[FieldEntry]] = memory.map { implicit memory =>
      val simpleFields: Seq[FieldEntry] = for {
        fieldDefinition <- fieldDefinitions.simpleFields
        it = fieldDefinition.iterator()
        metadata = fieldDefinition.keyMetadata
        number <- 1 to metadata.maxN
        fieldValue <- fieldDefinition.extractFromInts(it).toOption
      } yield {
        metadata match
          case KeyMetadata.Common =>
            Key(metadata, fieldDefinition.fieldName)

        val n = if(fieldDefinition.keyMetadata == KeyMetadata.Common)
          0 // todo this ids a hack, maybe handle in KeyKind definition or get rid of 0 as a magic number
        else
          number
        val key = Key(fieldDefinition.keyMetadata, n)
        val fieldEntry = FieldEntry(fieldDefinition, key, fieldValue)
        logger.trace("FieldEntry: offset: {} fieldEntry: {})", fieldDefinition.offset, fieldEntry.toString)
        fieldEntry
      }

      val complexFields: Seq[FieldEntry] = fieldDefinitions.complexFd.flatMap { memoryExtractor =>
        try {
          memoryExtractor.extract(memory)
        } catch {
          case e: Throwable =>
            logger.error(s"loading: ${memoryExtractor}", e)
            Seq.empty
        }
      }
      simpleFields ++: complexFields
    }
    r match
      case Failure(exception) =>
        logger.error("Loading from {} {}", memoryFile.toExternalForm, exception.getMessage)
      case Success(entries) => 
        logger.info("Loaded {} entries from {}", entries.size, memoryFile.toExternalForm)
    r
  }
}

object MemoryFileLoader {
  val notInitialized: Exception = new Exception("No RC-210 data!")
}



