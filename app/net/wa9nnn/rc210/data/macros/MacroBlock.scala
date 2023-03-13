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

import com.wa9nnn.util.tableui.Row
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.named.NamedManager

/**
 * Build function rows for a [[MacroNode]]
 * Prepends a column with MacroNode metadata key and name. etc.
 */
object MacroBlock {
  def apply(macroNode: MacroNode)(implicit namedManager: NamedManager,
                                  functionsProvider: FunctionsProvider): Seq[Row] = {

/*
    implicit val functions: Seq[FunctionKey] = macroNode.functions


 def buildRow(functionKey: FunctionKey, maybeMacroKey: Option[MacroKey] = None, last: Boolean = false): Row = {

      /**
       * Right extension td to a function td.
       */
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
            .withCssClass("flowMacroRight")
          if (last) {
            descriptionCell = descriptionCell.withCssClass("flowMacroBottom")
            destinationCell = destinationCell.withCssClass("flowMacroBottom")
          }
          Seq(descriptionCell, destinationCell)
        }.getOrElse(Seq.empty)
      }

      def macroCell() = {
        val macroKey = macroNode.key
        Cell(namedManager(macroKey))
          .withToolTip(s"Macro Command $macroKey")
          .withRowSpan(functions.length)
          .withCssClass("flowMacroCell bg-primary text-white")

      }

      def buildTriggersCell: Cell = {
        val macroKey = macroNode.key
        val triggerRows = rc210Data
          .triggers(macroKey)
          .map { triggerNode =>
            triggerNode.triggerRow
          }
        val triggersTable = Table(Seq.empty, triggerRows)
        val table = Table(
          headers = Seq.empty,
          rows = Seq(
            Row(Seq(
              TableInACell(triggersTable)
              //              Cell(namedManager(macroKey))
              //                .withToolTip(s"Macro Command ${macroKey.index}")
            )
            )
          ),
          cssClass = Seq.empty)
        TableInACell(table)
          .withRowSpan(functions.length)
          .withCssClass("flowMacroBottom flowMacroLeft flowMacroTop")
          .withToolTip(s"Macro Command ${macroKey.number}")

      }

      maybeMacroKey.map { macroKey =>
        val topRowCells = functionCells.map(_.withCssClass("flowMacroTop"))
        val moreCells: Seq[Cell] = macroCell() +: buildTriggersCell +: topRowCells
        new Row( moreCells)
      }.getOrElse {
        Row(functionCells)
      }
    }

    val functionRows = functions.length match {
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
    functionRows
//    functionRows :+ seperatorRow
*/
    Seq.empty //todo
  }


  //    .withCssClass("border-bottom border-3 border-secondary"))
  //  )
}
