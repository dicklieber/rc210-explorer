package net.wa9nnn.rc210.model.macros

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.bubble.NodeId
import net.wa9nnn.rc210.data.FunctionNodeId
import net.wa9nnn.rc210.model.Node

case class MacroNode(nodeId: MacroNodeId, dtmf: Option[String], functions: List[FunctionNodeId])
  extends RowSource with Node {
  private val functionsDisplay: String = functions.mkString(" ")

  override def toRow: Row = {
    Row(nodeId.toCell, dtmf.map(_.toString).getOrElse(" "), functionsDisplay)
  }


}

object MacroNode extends LazyLogging {
  val header: Header = Header("Macros", "Macro", "DTMF", "Functions")



}

case class MacroNodeId(override val number: Int) extends NodeId('m', number, "MacroNode")