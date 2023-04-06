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

import views.html.fieldDtmf

case class FieldDtmf(value: String) extends SimpleFieldValue {

  def toHtmlField(renderMetadata: RenderMetadata): String = {
    fieldDtmf(value, renderMetadata).toString()
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def display: String = value

  override def update(paramValue: String): FieldValue = {
    FieldDtmf(paramValue)
  }
}

object FieldDtmf extends FieldExtractor {
  override def extract(itr: Iterator[Int], simpleField: SimpleField): FieldDtmf = {
    val ints: Seq[Int] = for {
      n <- 0 to simpleField.max
    } yield {
      itr.next()
    }
    FieldDtmf(ints.takeWhile(_ != 0)
      .map(_.toChar)
      .toString())
  }
}