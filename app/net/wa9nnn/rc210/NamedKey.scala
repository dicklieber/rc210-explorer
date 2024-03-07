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
import net.wa9nnn.rc210.NamedKey.fieldName
import play.api.libs.json.{Format, Json}

case class NamedKey(key: Key, name: String) extends Ordered[NamedKey] with RowSource {
  override def compare(that: NamedKey): Int = key compareTo that.key

  override def toRow: Row = {
    Row(
      Cell(key.toString), name
    )
  }

  val fieldKey: FieldKey = FieldKey(key, fieldName)
}

object NamedKey {
  val fieldName = "keyName"
  implicit val fmtNamedKey: Format[NamedKey] = Json.format[NamedKey]
  
}

trait NamedKeySource {

  def nameForKey(key: Key): String

  def namedKey(key: Key): NamedKey =
    NamedKey(key, nameForKey(key))

  def namedKeys: Seq[NamedKey]
}


