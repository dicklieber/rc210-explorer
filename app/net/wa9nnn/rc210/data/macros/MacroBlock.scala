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

package net.wa9nnn.rc210.data.macros

import com.wa9nnn.util.tableui.{Cell, Row}
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.{FunctionKey, Key, MacroKey, MessageMacroKey}

/**
 * Build function rows for a [[MacroNode]]
 * Prepends a column with MacroNode metadata key and name. etc.
 */
object MacroBlock {
  def apply(macroNode: MacroNode)(implicit namedManager: NamedManager,
                                  rc210Data: Rc210Data,
                                  functionsProvider: FunctionsProvider): Seq[Row] = {

    implicit val functions: Seq[FunctionKey] = macroNode.functions

    functions.length match {
      case 0 =>
        Seq.empty
      case 1 =>
        Seq(buildRow(functions.head, Option(macroNode.key), last = true))
      case 2 =>
        Seq(
          buildRow(functions.head, Option(macroNode.key)),
          buildRow(functions(1), last = true)
        )
      case _ =>
        buildRow(functions.head, Option(macroNode.key)) +: functions
          .drop(1)
          .dropRight(1)
          .map(buildRow(_)) :+ buildRow(functions.last, last = true)
    }
  }

  /**
   * Builds one function row for the flow table.
   *
   * @param functionKey  to render
   * @param macroKey     if 1st will add top border. Can also be marked as last.
   * @param last         true if last or only row, will add bottom border
   * @return
   */
  def buildRow(functionKey: FunctionKey, macroKey: Option[MacroKey] = None, last: Boolean = false)
              (implicit functions: Seq[FunctionKey], functionsProvider: FunctionsProvider, namedManager: NamedManager, rc210Data: Rc210Data): Row = {
    val maybeMacroKeyCell: Option[Cell] = macroKey.map { mk =>
      Cell(namedManager(mk))
        .withRowSpan(functions.length)
        .withCssClass("bg-primary text-white border border-color-primary")
        .withToolTip(s"Macro Command ${mk.index}")
    }

    def buildDestinationCell(key: Option[Key]): Cell = {
      key
        .map {
          case key: MessageMacroKey =>
            rc210Data
              .messageMacroMap
              .get(key)
              .map(_.toCell)
              .getOrElse(Cell(""))

          case key =>
            key.toCell
        }.getOrElse(Cell(""))
    }


    val functionCells: Seq[Cell] = {
      functionsProvider(functionKey).map { functionNode =>
        var descriptionCell = Cell(functionNode.description)
        var destinationCell: Cell = buildDestinationCell(functionNode.destination)
          .withCssClass("border-right border-color-primary")
        if (last) {
          descriptionCell = descriptionCell.withCssClass("border-bottom border-color-primary")
          destinationCell = destinationCell.withCssClass("border-bottom border-color-primary")
        }
        Seq(descriptionCell, destinationCell)
      }.getOrElse(Seq.empty)
    }

    val cells: Seq[Cell] = maybeMacroKeyCell.map {
      _ +: functionCells
    }.getOrElse(functionCells)
    Row(cells)


  }
}
