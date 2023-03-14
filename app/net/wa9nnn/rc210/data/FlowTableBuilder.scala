package net.wa9nnn.rc210.data

import akka.actor.ActorRef
import com.wa9nnn.util.tableui._
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.MacroKey
import views.html.macroRowHeader

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FlowTableBuilder @Inject()(functions: FunctionsProvider, @Named("values-actor") valuesActor: ActorRef) (implicit ec: ExecutionContext, namedManager: NamedManager){
  def apply(): Table = {

/*
    val rows: Seq[Row] =  (valuesActor ? Values(KeyKindEnum.macroKey)).mapTo[Seq[MacroNode]].map{macroNode: Seq[MacroNode] =>
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
*/
    val header = Header("Flow", "Macro", "Triggers", "Functions")

    Table(header, Seq.empty)

  }
}

object FlowTableBuilder {
  def macroRowHeaderCell(macroKey: MacroKey): Cell = {
    Cell.rawHtml(
      macroRowHeader(macroKey).toString())
  }
}
