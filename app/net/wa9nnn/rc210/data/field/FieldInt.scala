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
import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.FormField
import play.api.libs.json.*

case class FieldInt(value: Int) extends FieldValueSimple():
  override def toRow: Row = Row(
    "FieldDtmf",
    toString
  )

  override def toEditCell(key:Key): Cell = FormField(key, value)

  override def toCommands(fieldEntry: FieldEntry): Seq[String] =
    val key = fieldEntry.key

    Seq(
      key.replaceN(fieldEntry.template)
      .replaceAll("v", value.toString)
    )


  override def displayCell: Cell = Cell(value)

  case class DefInt(offset: Int, fieldName: String, keyMetadata: KeyMetadata, override val template: String)
    extends FieldDefSimple[FieldInt]:

    override def fromFormField(value: String): FieldInt =
      FieldInt(value.toInt)

    override def extract(memory: Memory): Seq[FieldEntry] =
     FieldInt( memory.iterator(offset))

    override def positions: Seq[FieldOffset] = ???

    override def extractFromInts(itr: Iterator[Int], fieldDefinition: FieldDefSimple): FieldValue = {
      val ints: Seq[Int] = for
      {
        _ <- 0 to fieldDefinition.max
      } yield
      {
        itr.next()
      }

      val tt: Array[Char] = ints.takeWhile(_ != 0)
        .map(_.toChar).toArray
      val str: String = new String(tt)
      new FieldDtmf(str)
    }


