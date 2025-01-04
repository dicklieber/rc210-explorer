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
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.ui.{FormData, FormField}
import play.api.libs.json.*

case class Field2Numbers(value: Seq[Int]) extends FieldValueSimple():

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(key: Key, template: String): String =
    key.replaceN(template)
      .replaceAll("v", value.map(int => f"$int%03d").mkString(""))

  override def toEditCell(key: Key): Cell =
    FormField(key, value)

  override def displayCell: Cell =
    Cell(value.map(_.toString).mkString(" "))

  override def toRow(fieldEntry: FieldEntry): Row = Row(
    fieldEntry.fieldDefinition.fieldName,
    toString
  )

case class Def2Numbers(offset: Int, fieldName: String, keyMetadata: KeyMetadata, override val template: String)
  extends FieldDefSimple[Field2Numbers]:

  implicit val fmt: OFormat[Field2Numbers] = Json.format[Field2Numbers]

  def extract(iterator: Iterator[Int]): Field2Numbers =
    Field2Numbers(Seq(iterator.next(), iterator.next()))

  override def fromString(str: String): Field2Numbers =
    update(str)

  def update(str: String): Field2Numbers =
    val values: Seq[Int] = if (str.isBlank)
      Seq(0, 0)
    else
      str
        .split(" ")
        .toIndexedSeq
        .map(_.toInt)
    Field2Numbers(values)
    
  


