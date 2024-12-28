package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.data.field.{FieldDef, FieldOffset, FieldValue}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.FormData
import play.api.libs.json.{Format, Json}

import scala.collection.immutable
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

trait FieldDefSimple[T <: FieldValue] extends FieldDef[T]:
  /**
   * to make RC-210 commands.
   * Only needed for [[FieldDefSimple]] as [[FieldDefComplex]] builds these into the Complex field definetions.
   */
  val template: String

  def fromFormField(value: String): T

  def fromForm(formData: FormData): Seq[UpdateCandidate] =
    formData.keyedValues.map { case (key, value) =>
      val value1: T = fromFormField(value)
      UpdateCandidate(key, value1)
    }

/*
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
  private val fmt1: Format[FieldValue] = fieldExtractor.fmt
  val fmt = fmt1
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
*/


