package net.wa9nnn.rc210.model.macros

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.bubble.{D3Link, D3Node, NodeId}
import net.wa9nnn.rc210.data.FunctionNodeId
import net.wa9nnn.rc210.model.{DatFile, DataItem, Node}

import scala.util.Try

case class Macro(nodeId: MacroNodeId, dtmf: Option[String], functions: List[FunctionNodeId]) extends RowSource with Node {
  private val functionsDisplay: String = functions.mkString(" ")

  override def toRow: Row = {
    Row(nodeId.toString, dtmf.map(_.toString).getOrElse(" "), functionsDisplay)
  }

  /**
   * What this node can invoke.
   */
  override val outGoing: Seq[NodeId] = functions

  override def d3Node: D3Node = {

    D3Node(nodeId, functionsDisplay, functions.map(D3Link(nodeId, _)))
  }
}

object Macro extends LazyLogging {
  val header: Header = Header("Macros", "Macro", "DTMF", "Functions")



}

case class MacroNodeId(override val number: Int) extends NodeId('m', number)