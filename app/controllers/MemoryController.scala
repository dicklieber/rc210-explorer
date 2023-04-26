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

package controllers

import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.{DataStore, MemoryFileLoader}
import net.wa9nnn.rc210.data.field.{FieldDefinition, FieldDefinitions, FieldEntry, FieldOffset}
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.FormParser
import play.api.mvc._

import javax.inject.Inject

class MemoryController @Inject()(memoryFileLoader: MemoryFileLoader, fieldDefinitions: FieldDefinitions)(implicit val controllerComponents: ControllerComponents)
  extends BaseController {

  private lazy val offsetToField: Map[Int, FieldDefinition] =
    (for {
      fd <- fieldDefinitions.allFields
      position <- fd.positions
    } yield {
      position.offset -> fd
    }).toMap

  def index: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val memory: Memory = memoryFileLoader.loadMemory
      val rows: Seq[Row] = memory.data.zipWithIndex.map { case (int, index) =>
        val row = Row(Cell(index), Cell(int))
        offsetToField.get(index).map { fieldDefinition =>
          val extraCells: Seq[Cell] = Seq(
            fieldDefinition.kind,
            fieldDefinition.fieldName,
            fieldDefinition.template
          ).map(Cell(_))
          row.copy(cells = row.cells :++ extraCells)
        }.getOrElse(row)

      }
      val header = Header("RC-210 Memory Map", "Offset", "Value", "Kind", "Field Name", "Command Template")
      val table = Table(header, rows)

      Ok(views.html.dat(Seq(table)))
  }

}
