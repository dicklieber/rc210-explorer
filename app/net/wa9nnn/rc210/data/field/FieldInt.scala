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
  override def toRow(fieldEntry: FieldEntry): Row = Row(
    fieldEntry.fieldDefinition.fieldName,
    value.toString
  )

  override def toEditCell(key: Key): Cell = FormField(key, value)

  override def toCommand(key: Key, template: String): String =
    key.replaceN(template)
      .replaceAll("v", value.toString)

  override def displayCell: Cell = Cell(value)

case class DefInt(offset: Int, 
                  fieldName: String, 
                  keyMetadata: KeyMetadata, 
                  template: String, 
                  units: String = "",
                  max: Int = Int.MaxValue,
                 )
  extends FieldDefSimple[FieldInt]:
  def units(units: String): DefInt =
    copy(units = units)
  def max(max:Int): DefInt =
    copy(max = max)

  override def fromString(str: String): FieldInt =
    FieldInt(str.toInt)

  override def fmt: Format[FieldInt] = new Format[FieldInt]:
    override def reads(json: JsValue) =
      JsSuccess(FieldInt(json.as[Int]))

    override def writes(o: FieldInt) =
      JsNumber(o.value)

  override def extract(iterator: Iterator[Int]): FieldValueSimple =
    FieldInt(iterator.next())




