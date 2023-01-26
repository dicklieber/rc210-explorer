package net.wa9nnn.rc210.bubble

import com.wa9nnn.util.HostAndPort
import com.wa9nnn.util.tableui.{Cell, CellProvider, Row, RowSource}
import net.wa9nnn.rc210.data.{FunctionNodeId, ScheduleNodeId}
import net.wa9nnn.rc210.model.Node
import net.wa9nnn.rc210.model.macros.MacroNodeId
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

/**
 * Unique ID for any node.
 *
 * @param prefix kind of node e.g. 'm' macro 'f' function 't' timer
 * @param number within the kind of [[Node]]
 */
abstract class NodeId(prefix: Char, val number: Int, cssClass: String) extends CellProvider {
  override def toString: String = {
    s"$prefix$number"
  }

  def toCell: Cell = Cell(number).withCssClass(cssClass)
}

object NodeId {
  implicit val nodeIdFormat: Format[NodeId] = new Format[NodeId] {
    override def reads(json: JsValue): JsResult[NodeId] = {

      try {
        JsSuccess(NodeId(json.as[String]))
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(nodeId: NodeId): JsValue = {
      JsString(nodeId.toString)
    }
  }

  def apply(sNodeId: String): NodeId = {
    sNodeId.head match {
      case 'f' => FunctionNodeId(sNodeId.tail.toInt)
      case 'm' => MacroNodeId(sNodeId.tail.toInt)
      case 's' => ScheduleNodeId(sNodeId.tail.toInt)
    }
  }


}

trait TriggerNode extends Node with RowSource {
  val macroToRun: MacroNodeId

  def description: String

}

