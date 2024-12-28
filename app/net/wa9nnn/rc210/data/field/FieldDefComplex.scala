package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.field.{FieldDef, FieldEntry, FieldValueComplex}
import net.wa9nnn.rc210.serial.Memory
import play.api.data.Form
import play.api.libs.json.Format

/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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

trait FieldDefComplex[T <: FieldValueComplex[?]] extends FieldDef[T] with EditHandler :

  override def fieldName: String = keyMetadata.entryName


  def form: Form[T]
  val fmt: Format[T]


