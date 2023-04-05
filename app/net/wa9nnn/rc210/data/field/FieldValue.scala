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

package net.wa9nnn.rc210.data.field

import com.wa9nnn.util.tableui.{Cell, Row}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.key.KeyFactory.Key

/**
 * Holds the value for a field.
 * Knows how to render as HTML control or string for JSON, showing to a user or RC-210 Command,
 * Has enough metadata needed yo render
 */
sealed trait FieldValue {

  def display: String

  /**
   * Render this value as an RD-210 command string.
   */
  def toCommand(fieldEntry: FieldEntry): String

  /**
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   * @param fieldEntry all the metadata.
   * @return html
   */

  def toHtmlField(renderMetadata: RenderMetadata): String


  /**
   *
   * @param paramValue candidate from form.
   * @return None if value has not changed, otherwise a new [[FieldValue]].
   */
  def update(paramValue: String): FieldValue
}


/**
 * Renders itself as a [[[Cell]]
 */
trait SimpleFieldValue extends FieldValue{
  def toCell(renderMetadata: RenderMetadata): Cell = {
    val html: String = toHtmlField(renderMetadata)
    Cell.rawHtml(html)
  }

}

/**
 *  Renders itself as a [[Row]]
 *
 * @tparam K
 */
trait ComplexFieldValue[K <: Key] extends FieldValue {
  val key: K
  val fieldName: String
  lazy val fieldKey: FieldKey = FieldKey(fieldName, key)

  /**
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   * @param fieldEntry all the metadata.
   * @return html
   */
  override def toHtmlField(renderMetadata: RenderMetadata): String = ???

  def toRow()(implicit namedSource: NamedSource): Row

  override def update(paramValue: String): FieldValue = throw new IllegalStateException("FieldWithFieldKey cannot be updated. ") //todo can it?
}

trait FieldExtractor[T <: FieldValue] {
  def extract(itr: Iterator[Int], field:SimpleField): T
}








