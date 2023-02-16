package net.wa9nnn.rc210.data

import com.wa9nnn.util.tableui.{Cell, Header, Row, Table, TableInACell}
import net.wa9nnn.rc210.data.FlowTableBuilder.macroRowHeaderCell
import net.wa9nnn.rc210.{DataProvider, MacroKey}
import net.wa9nnn.rc210.data.functions.{FunctionNode, FunctionsProvider}
import net.wa9nnn.rc210.model.TriggerNode
import views.html.macroRowHeader

import javax.inject.{Inject, Singleton}

@Singleton
class FlowTableBuilder @Inject()(functions: FunctionsProvider, dataProvider: DataProvider) {
  def apply(): Table = {

    val rc210Data = dataProvider.rc210Data
    val rows: Seq[Row] = rc210Data.macros.map { macroNode =>
      val macroFunctionsTable: Table = {
        val functionRows = macroNode.functions.flatMap { functionKey =>
          functions(functionKey).map(_.toRow)
        }
        Table(FunctionNode.header(functionRows.length), functionRows)
      }
      val triggerTable: Table = {
        val triggers: Seq[TriggerNode] = rc210Data.triggers(macroNode.key)

        Table(TriggerNode.header(triggers.length), triggers.map(_.triggerRow))
      }
      val triggersCell: Cell = TableInACell(triggerTable)
      val macroFunctionTable: Cell = TableInACell(macroFunctionsTable)
      Row(Seq( macroRowHeaderCell(macroNode.key),  triggersCell, macroFunctionTable))
    }
    val header = Header("Flow", "Macro", "Triggers", "Functions")

    Table(header, rows)

  }
}

object FlowTableBuilder {
  def macroRowHeaderCell(macroKey: MacroKey): Cell = {
    Cell.rawHtml(
      macroRowHeader(macroKey).toString())
  }
}
