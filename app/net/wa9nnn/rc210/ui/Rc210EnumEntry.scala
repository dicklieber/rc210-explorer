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

package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row}
import enumeratum.EnumEntry
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.field.{FieldDefSimple, FieldEntry, FieldValueSimple}

/**
 * A [[FieldValue]] that is an Enumeratium entry.
 */
abstract class Rc210EnumEntry extends FieldValueSimple() with EnumEntry:
  val rc210Value: Int
  val vals: Seq[Rc210EnumEntry]
  logger.trace(s"rc210Value: $rc210Value")
  logger.trace(s"vals: $vals")

  def options: Seq[(String, String)] = vals.map(v =>
    val s = v.entryName
    s -> s
  )

  override def toRow(fieldEntry: FieldEntry): Row =
    Row(
      fieldEntry.fieldDefinition.fieldName,
      entryName
    )

  override def displayCell: Cell = Cell(entryName)

  override def toEditCell(key: Key): Cell =
    FormField(key, this)

  override def toCommand(key: Key, template: String): String =
    key.replaceN(template)
      .replaceAll("v", rc210Value.toString)


