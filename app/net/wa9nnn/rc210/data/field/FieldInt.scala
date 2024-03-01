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
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.ui.FormField
import play.api.libs.json.*

case class FieldInt(value: Int) extends SimpleFieldValue():
  override def toRow: Row = Row(
    "FieldDtmf",
    toString
  )

  override def toEditCell(fieldKey: FieldKey): Cell = FormField(fieldKey, value)

  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val fieldKey = fieldEntry.fieldKey

    Seq(fieldKey.key.replaceN(fieldEntry.template)
      .replaceAll("v", value.toString))

  }

  override def displayCell: Cell = Cell(value)

  override def update(paramValue: String): FieldInt = {
    FieldInt(paramValue.toInt)
  }

  override def toJsValue: JsValue = Json.toJson(this)

object FieldInt extends SimpleExtractor:

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): FieldInt = {
    new FieldInt(if (field.max > 256)
      itr.next() + itr.next() * 256
    else
      itr.next()
    )
  }

  implicit val fmtFieldInt: Format[FieldInt] = new Format[FieldInt] {
    override def reads(json: JsValue): JsResult[FieldInt] = JsSuccess(new FieldInt(json.as[Int]))

    override def writes(o: FieldInt): JsValue = Json.toJson(o.value)
  }

  override def parse(jsValue: JsValue): FieldValue = new FieldInt(jsValue.as[Int])

