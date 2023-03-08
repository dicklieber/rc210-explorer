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

import akka.actor.ActorRef
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.ValuesActor.InitialData
import net.wa9nnn.rc210.data.field.{ExtractResult, FieldDefinitions, FieldEntry, FieldMetadata, FieldValue}
import net.wa9nnn.rc210.data.macros.MacroExtractor
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.schedules.ScheduleExtractor
import net.wa9nnn.rc210.data.vocabulary.MessageMacroExtractor
import net.wa9nnn.rc210.data.{FieldKey, Rc210Data}
import net.wa9nnn.rc210.key.{KeyFactory, KeyFormats}
import net.wa9nnn.rc210.serial.{Memory, MemoryArray}

import java.io.InputStream
import javax.inject.{Inject, Named, Singleton}
import scala.util.{Failure, Success, Using}

@Singleton
class DataProvider @Inject()(@Named("values-actor") valuesActor: ActorRef) extends LazyLogging {


  val rc210Data: Rc210Data = (Using(getClass.getResourceAsStream("/MemFixedtxt.txt")) {
    stream: InputStream =>
      val memory: Memory = MemoryArray(stream).get
      //      val mappedValues: MappedValues = new MappedValues()
      var rc210Data: Rc210Data = Rc210Data()

      FieldDefinitions.fields.foreach { fieldMetadata: FieldMetadata =>
        var start = fieldMetadata.offset
        val r: Seq[FieldEntry] = for {
          n <- 1 to fieldMetadata.kind.maxN
        } yield {
          val fieldKey: FieldKey = new FieldKey(fieldMetadata.fieldName, KeyFactory(fieldMetadata.kind, n))
          val extractResult: ExtractResult = fieldMetadata.extract(memory, start)
          start = extractResult.newOffset

          FieldEntry(FieldValue(fieldKey, extractResult.value), fieldMetadata)
        }
        valuesActor ! InitialData(r)
      }
      rc210Data
  } match {
    case Failure(exception) =>
      logger.error("Initial loading", exception)
      Rc210Data()
    case Success(value: Rc210Data) =>
      value
  })
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
