package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{ Key, KeyMetadata}
import net.wa9nnn.rc210.data.field.{FieldDef, FieldOffset, FieldValue, SimpleExtractor}
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.{Format, Json}

import scala.util.Try

/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */

/**
 * A [[FieldDefSimple]] produces one RC-210 command as opposed to a complex rc2input like [[net.wa9nnn.rc210.data.schedules.ScheduleNode]] that may produce multiple commands.
 * And generally will be an HTML form itself to edit.
 *
 * @param offset         where in [[Memory]] this comes from.
 * @param fieldName      as shown to users.
 * @param keyMetadata
 * @param template       used to generate the rc-210 command.
 * @param fieldExtractor that knows how to get this from the [[net.wa9nnn.rc210.serial.Memory]]
 * @param tooltip        for this rc2input.
 * @param units          suffix for <input>
 * @param max            used by the extractor. e.g. max DtMF digits or max number.
 */
case class FieldDefSimple(offset: Int,
                          fieldName: String,
                          val keyMetadata: KeyMetadata,
                          override val template: String,
                          fieldExtractor: SimpleExtractor[?],
                          override val tooltip: String = "",
                          override val units: String = "",
                          min: Int = 1,
                          max: Int = 255,
                                ) extends FieldDef with LazyLogging :
  
  def extractFromInts(iterator: Iterator[Int]): Try[FieldValue] = {
    val tried: Try[FieldValue] = Try {
      fieldExtractor.extractFromInts(iterator, this)
    }
    if (tried.isFailure)
      logger.error(s"Extracting: $this. Ignored!")
    tried
  }

  /**
   * Create an [[Iterator[Int]] over the [[Memory]] starting at an offset.
   *
   * @param memoryBuffer data from RC-210 binary dump.
   */
  def iterator()(implicit memoryBuffer: Memory): Iterator[Int] = {
    if (max > 255)
      memoryBuffer.iterator16At(offset)
    else
      memoryBuffer.iterator8At(offset)
  }

  def units(u: String): FieldDefSimple = copy(units = u)

  def max(max: Int): FieldDefSimple = copy(max = max)

  def min(min: Int): FieldDefSimple = copy(min = min)

  def tooltip(tooltip: String): FieldDefSimple = copy(tooltip = tooltip)


  override def positions: Seq[FieldOffset] = Seq(FieldOffset(offset, this))


