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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row}
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.ui.FormField
import play.api.libs.json.*

case class Field2Numbers(value: Seq[Int]) extends FieldValueSimple():

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: TemplateSource): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey
    val key: Key = fieldKey.key
    Seq(key.replaceN(fieldEntry.template)
      .replaceAll("v", value.map(int => f"$int%03d").mkString(""))
    )
  }

  override def update(formFieldValue: String): Field2Numbers = {
    val candidate: Seq[Int] = if (formFieldValue.isBlank)
      Seq(0, 0)
    else
      formFieldValue
        .split(" ")
        .toIndexedSeq
        .map(_.toInt)
    copy(value = candidate)
  }

  override def toJsValue: JsValue = Json.toJson(this)

  override def displayCell: Cell =
    Cell(value.map(_.toString).mkString(" "))


  override def toRow: Row = Row(
    "Field2Numbers",
    toString
  )

object Field2Numbers extends SimpleExtractor:

  implicit val fmt: Format[Field2Numbers] = Json.format[Field2Numbers]

  override def extractFromInts(iterator: Iterator[Int], fieldDefinition: FieldDefSimple): FieldValue = {
    Field2Numbers(Seq(iterator.next(), iterator.next()))
  }


