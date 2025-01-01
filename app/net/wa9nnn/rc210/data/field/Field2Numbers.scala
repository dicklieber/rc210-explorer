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
import Field2Numbers.update

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

  override def toRow: Row = Row(
    "Field2Numbers",
    toString
  )

case class FieldDef2Numbers(offset: Int, fieldName: String, keyMetadata: KeyMetadata, override val template: String)
  extends FieldDefSimple[Field2Numbers]:

  override val fmt: Format[Field2Numbers] = Field2Numbers.fmt

  override def extract(memory: Memory): Seq[FieldEntry] = ???

  override def fromFormField(value: String): Field2Numbers =
    update(value)
    

object Field2Numbers :

  val fmt: Format[Field2Numbers] = Json.format[Field2Numbers]

//  override def extractFromInts(iterator: Iterator[Int], fieldDefinition: FieldDefSimple): FieldValue =
//    Field2Numbers(Seq(iterator.next(), iterator.next()))

  def update(formFieldValue: String): Field2Numbers =
    val values: Seq[Int] = if (formFieldValue.isBlank)
      Seq(0, 0)
    else
      formFieldValue
        .split(" ")
        .toIndexedSeq
        .map(_.toInt)
    Field2Numbers(values)
  
    
  


