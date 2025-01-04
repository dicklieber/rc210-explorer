/*
 * Copyright (c) 2025. Dick Lieber, WA9NNN
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
 *
 */

package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.KeyMetadata
import play.api.libs.json.Format
import net.wa9nnn.rc210.ui.{Rc210Enum, Rc210EnumEntry}

import scala.reflect.ClassTag

case class FieldEnum(value: Rc210EnumEntry)

case class DefFieldEnum[T <: Rc210EnumEntry](offset: Int, fieldName: String,
                                             keyMetadata: KeyMetadata,
                                             template: String,
                                             rc210Enum: Rc210Enum[T]
                                            ) extends FieldDefSimple[T]:
  
  override def fromString(str: String): T =
    val fe: T = rc210Enum.withName(str)
    FieldEnum(fe).asInstanceOf[T]

  override def extract(iterator: Iterator[Int]): FieldValueSimple =
    val int = iterator.next()
    rc210Enum.find(int)

  override def fmt: Format[T] =
    rc210Enum.jsonFormat

