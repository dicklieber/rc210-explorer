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

package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.serial.{Memory, MemoryArray, Slice}

import java.io.InputStream
import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Using}

@Singleton
class DataProvider @Inject()(fieldDefinitions: FieldDefinitions) extends LazyLogging {

  implicit val memory: MemoryArray = Using(getClass.getResourceAsStream("/MemFixedtxt.txt")) {
    stream: InputStream =>
      MemoryArray(stream).get
  } match {
    case Failure(exception) =>
      logger.error("Initial loading", exception)
      throw new Exception()
    case Success(value: MemoryArray) =>
      logger.debug(s"Initial loaded ${value.data.length} bytes from: /MemFixedtxt.txt")
      value
  }


  private val simpleFields: Seq[FieldEntry] = for {
    fieldDefinition <- fieldDefinitions.simplefields
    number <- 1 to fieldDefinition.kind.maxN
  } yield {
    val start = fieldDefinition.offset + fieldDefinition.uiInfo.fieldExtractor.bytesPerField * (number - 1)
    val (fieldContents: FieldValue, slice: Slice) = fieldDefinition.extract(start)
    val fieldKey = fieldDefinition.fieldKey(number)
    val fieldEntry: FieldEntry = FieldEntry(fieldDefinition, fieldKey, fieldContents) //todo deal with candidate.
    logger.trace("FieldEntry: start: {}  slice: {} fieldEntry: {}", start.toString, slice.toString, fieldEntry.toString)
    fieldEntry
  }

  private val values: Seq[FieldEntry] = fieldDefinitions.complexFd.flatMap { memoryExtractor: MemoryExtractor =>
    val r: Seq[FieldEntry] = memoryExtractor.extract(memory)
    r
  }

  val initialValues: Seq[FieldEntry] = simpleFields ++: values
}


trait MemoryExtractor extends FieldDefinition {

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  def extract(memory: Memory): Seq[FieldEntry]
}
