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
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.serial.Memory

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success}

@Singleton
class MemoryFileLoader @Inject()(fieldDefinitions: FieldDefinitions, datFile: DatFile) extends LazyLogging {

  /**
   * Get sacved Memory (Raw RC-210 download data.)
    * @return
   */
  def loadMemory: Memory = {
    Memory.load(datFile.memoryFile) match {
      case Failure(exception) =>
        logger.error(s"No Memory", exception)
        throw exception
      case Success(value) =>
        value
    }
  }


  def load: Seq[FieldEntry] = {
    implicit val memory = loadMemory
    val simpleFields: Seq[FieldEntry] = for {
      fieldDefinition <- fieldDefinitions.simpleFields
      it = fieldDefinition.iterator()
      number <- 1 to fieldDefinition.kind.maxN
      fieldValue <- fieldDefinition.extractFromInts(it).toOption
    } yield {
      val fieldKey = fieldDefinition.fieldKey(number)
      val fieldEntry = FieldEntry(fieldDefinition, fieldKey, fieldValue)
      logger.trace("FieldEntry: offset: {} fieldEntry: {})", fieldDefinition.offset, fieldEntry.toString)
      fieldEntry
    }

    val complexFields: Seq[FieldEntry] = fieldDefinitions.complexFd.flatMap { memoryExtractor: ComplexExtractor =>
      try {
        memoryExtractor.extract(memory)
      } catch {
        case e: Throwable =>
          logger.error(s"loading: ${memoryExtractor.fieldName}", e)
          Seq.empty
      }
    }

    simpleFields ++: complexFields
  }
}



