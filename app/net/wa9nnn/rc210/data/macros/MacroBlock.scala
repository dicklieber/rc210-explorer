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

import com.wa9nnn.wa9nnnutil.tableui._

/**
 * Build function rows for a [[MacroNode]]
 * Prepends a column with MacroNode metadata key and name. etc.
 */
object MacroBlock {
//  def apply(macroWithTriggers: MacroWithTriggers)(implicit actor: ActorRef[DataStoreActor.Message], functionsProvider: FunctionsProvider): Row = {
//    val macroKey = macroWithTriggers.macroNode.key
//    val macroCell: Cell = macroKey.toCell
//      .withCssClass("flowMarcoCell")
//
//    val triggersCell: Cell = {
//      var triggerRows: Seq[Row] = macroWithTriggers.triggers.map(_.toRow)
//        val value: FieldValue = fieldEntry.value
//        Row(fieldEntry.fieldKey.toCell, value.display)
//      }
//      macroNode.dtmf.foreach { dtmf =>
//        triggerRows = triggerRows.prepended(Row("DTMF", dtmf.toString))
//      }
//
//      if (macroKey.number == 1) {
//        val cell = Cell("--Start--")
//          .withColSpan(2)
//        val startRow = new Row(Seq(cell))
//
//        triggerRows = triggerRows.prepended(startRow)
//      }
//
//      val triggersTable = Table(Seq.empty, triggerRows)
//      TableInACell(triggersTable)
//        .withCssClass("flowMacroBottom flowMacroLeft flowMacroTop")
//        .withToolTip(s"Macro Command ${macroKey.number}")
//
//    }.withCssClass("flowTriggersCell")
//
//    val functionTableCell: Cell = {
//      val functionRows = for {
//        functionKey <- macroNode.functions
//        functionNode <- functionsProvider(functionKey)
//      } yield {
//        Row(functionKey.toCell, functionNode.description)
//      }.withCssClass("flowFunctionsCell")
//      TableInACell(Table(Seq.empty, functionRows))
//    }
//    Row(triggersCell, macroCell, functionTableCell)
//  }


  private val cellSeperator: Cell = Cell("seperator")
    .withColSpan(3)
  //    .withCssClass("border-bottom border-3 border-secondary"))
  //  )
  val seperatorRow: Row = Row(Seq(cellSeperator))
}

