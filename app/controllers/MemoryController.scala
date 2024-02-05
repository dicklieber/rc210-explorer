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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.MemoryFileLoader
import net.wa9nnn.rc210.data.field.{FieldDefinition, FieldDefinitions, FieldOffset}
import net.wa9nnn.rc210.ui.nav.TabKind.Rc210Io
import net.wa9nnn.rc210.ui.Tabs
import play.api.mvc.*

import javax.inject.Inject
import scala.util.{Failure, Success}

class MemoryController @Inject()(memoryFileLoader: MemoryFileLoader, fieldDefinitions: FieldDefinitions)(implicit val controllerComponents: ControllerComponents)
  extends BaseController {

  private lazy val offsetToField: Map[Int, Seq[Cell]] =
    (for {
      fd: FieldDefinition <- fieldDefinitions.allFields
      position: FieldOffset <- fd.positions
    } yield {
      val extraCells: Seq[Cell] = Seq(
        fd.keyKind,
        fd.fieldName,
        position.field.getOrElse(""),
        fd.template
      ).map(Cell(_))

      position.offset -> extraCells
    }).toMap

  def index: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      memoryFileLoader.memory match {
        case Failure(exception) =>
          NotFound("No Saved RC-210 memory. DownloadActor from RC-210.")
        case Success(memory) => val rows: Seq[Row] = memory.data.zipWithIndex.toIndexedSeq.map { case (int, index) =>
          val row = Row(Cell(index.toString), Cell(f"$int 0x${int.toHexString}"))
          offsetToField.get(index).map { extraCells =>
            row.copy(cells = row.cells :++ extraCells)
          }.getOrElse(row)

        }
          val header = Header("RC-210 Memory Map", "Offset", "Value", "Kind", "Field Name", "Field", "Command Template")
          val table = Table(header, rows)

          Ok(views.html.justdat(Tabs.memory, Seq(table)))

      }
  }

}
