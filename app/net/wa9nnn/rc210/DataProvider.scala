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
import net.wa9nnn.rc210.data.{FieldKey, Rc210Data}
import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.serial.{Memory, MemoryArray}

import java.io.InputStream
import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Using}

@Singleton
class DataProvider @Inject()() extends LazyLogging {


  val memory: MemoryArray = (Using(getClass.getResourceAsStream("/MemFixedtxt.txt"))) {
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

  //  //      val mappedValues: MappedValues = new MappedValues()
  //  var rc210Data: Rc210Data = Rc210Data()

  private var start = FieldDefinitions.fields.head.offset
  val ife: Seq[FieldEntry] = for {
    fieldMetadata <- FieldDefinitions.fields
    number <- 1 to fieldMetadata.kind.maxN
  } yield {
    val extractResult: ExtractResult = fieldMetadata.extract(memory, start)
    start = extractResult.newOffset // move to next position in memory
    val fieldKey: FieldKey = new FieldKey(fieldMetadata.fieldName, KeyFactory(fieldMetadata.kind, number))
    FieldEntry(FieldValue(fieldKey, extractResult.value), fieldMetadata)
  }
  var rc210Data: Rc210Data = Rc210Data() //todo get rid of Rc210Data.
}


trait MemoryExtractor {
  /**
   *
   * @param memory    source of RC-210 data.
   * @param rc210Data internal to have our data appended to it.
   * @return the inputted rc210Data with our data inserted into it.
   */
  def apply(memory: Memory, rc210Data: Rc210Data): Rc210Data
}
