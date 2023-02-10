package net.wa9nnn.rc210.data

import com.wa9nnn.util.tableui.{Cell, Header, Row, Table, TableInACell}
import net.wa9nnn.rc210.bubble.TriggerNode
import net.wa9nnn.rc210.data.BuilderHelpers._
import net.wa9nnn.rc210.model.DatFile
import net.wa9nnn.rc210.model.macros.{MacroNode, MacroNodeId}

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@Singleton
class FlowTableBuilder @Inject()(functions: Functions) {
  def aoply(datFile: DatFile): Table = {

    val macroToTriggers: mutable.Map[MacroNodeId, ListBuffer[TriggerNode]] = BuilderHelpers(datFile)

    val maroRows: Seq[Row] = datFile.macros.map { macroNode =>
      val triggersFOrThisNode: collection.Seq[TriggerNode] = macroToTriggers.getOrElse(macroNode.nodeId, Seq.empty)
      val triggerRows: Seq[Row] = triggersFOrThisNode.map {
        _.toRow
      }.toSeq
      val tiggersTable =  if(triggerRows.nonEmpty){
        Table(header, triggerRows)
      }else{
        Table(Seq.empty, Seq.empty)
      }

      val functionRows: Seq[Row] = macroNode.functions.flatMap { functionNodeId =>
        functions.get(functionNodeId).map(_.toRow)
      }
      val functionsTable = Table(header, functionRows)
      Row(TableInACell(tiggersTable), TableInACell(functionsTable))
    }
    Table(Header("Flow", "Triggers", "Functions"), maroRows)
  }

  def macroTable(macroNode: MacroNode): Table = {
    val headerRow = Row(Seq(Cell(s"Macro: ${
      macroNode.nodeId.number
    }")
      .withColSpan(2)))

    val functionRows = macroNode.functions.flatMap {
      f: FunctionNodeId =>
        functions.get(f).map(_.toRow)
    }
    val header = Header(s"Functions for Macro: ${
      macroNode.nodeId.number
    }", "Function", "Description")
    Table(header, functionRows)
  }

}

object BuilderHelpers {
  /**
   * find all the triggers for each MacroNode
   */
  def apply(datFile: DatFile): mutable.Map[MacroNodeId, ListBuffer[TriggerNode]] = {

    val macro2Triggers: mutable.Map[MacroNodeId, ListBuffer[TriggerNode]] = new TrieMap[MacroNodeId, ListBuffer[TriggerNode]]()

    def appendTrigger(triggerNode: TriggerNode): Unit = {
      macro2Triggers.getOrElseUpdate(triggerNode.macroToRun, ListBuffer.empty[TriggerNode])
        .append(triggerNode)
    }

    datFile.schedules.foreach(appendTrigger(_))

    macro2Triggers
  }

  val header:Header =  Header.singleRow("Number", "Description")
}
