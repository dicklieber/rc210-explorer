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

package net.wa9nnn.rc210

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row, RowSource}
import net.wa9nnn.rc210.ui.{KeyAndValues, KeyedRow}
import play.api.libs.json.{Format, Json}

case class NamedKey(key: Key, name: String) extends KeyedRow:

  override def toRow(key: Key): Row =
    Row(
      Cell(key.toString), name
    )

object NamedKey:
  def apply(key: Key): NamedKey = NamedKey(key, "")
  def apply(keyValues:KeyAndValues): NamedKey =
    NamedKey(keyValues.key, keyValues.oneValue)

  given ordering: Ordering[NamedKey] = Ordering.by(_.key)

  val fieldName = "keyName"
  implicit val fmtNamedKey: Format[NamedKey] = Json.format[NamedKey]





