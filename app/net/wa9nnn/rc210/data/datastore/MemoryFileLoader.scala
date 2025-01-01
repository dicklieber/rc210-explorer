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
import net.wa9nnn.rc210.data.field.{FieldDefComplex, FieldDefSimple, FieldDefinitions, FieldEntry}
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
class MemoryFileLoader @Inject()(fieldDefinitions: FieldDefinitions)(implicit config: Config) extends LazyLogging :
  private val memoryFilePath: Path = path("vizRc210.memoryFile")
  private val memoryFile: URL = memoryFilePath.toUri.toURL

  private var tryMemory: Try[Memory] = Failure(notInitialized)

  def memory: Try[Memory] = tryMemory

  /**
   * Extract RC-210 values from [[Memory]]
   * for loading into the [[DataStore]].
   */
  def extractFieldEntries(): Seq[FieldEntry] =
    tryMemory = Memory.load(memoryFile)
    memory match
      case Failure(exception) =>
        logger.error("Loading", exception)
        Seq.empty
      case Success(memory) =>
        val r =
        {
          fieldDefinitions.extractors.flatMap { fieldDefinition =>
            val fieldName = fieldDefinition.fieldName
            val metadata = fieldDefinition.keyMetadata
            fieldDefinition match
              case complex: FieldDefComplex[_] =>
                complex.extract(memory)
              case simple: FieldDefSimple[_] =>
                val iterator = memory.iterator(simple.offset)
                for
                  n <- 1 to simple.keyMetadata.maxN
                yield
                  val key = Key(metadata, n, fieldName)
                  val value = simple.extract(iterator)
                  FieldEntry(simple, key, value)
              case x =>
                throw new IllegalStateException(s"Unknown fieldDefinition: $x")
          }
        }
        r

  object MemoryFileLoader:
    val notInitialized: Exception = new Exception("No RC-210 data!")
