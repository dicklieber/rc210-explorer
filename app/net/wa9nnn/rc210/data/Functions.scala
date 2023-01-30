package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.bubble.NodeId
import net.wa9nnn.rc210.data.FunctionNode._
import net.wa9nnn.rc210.model.Node
import net.wa9nnn.rc210.model.macros.MacroNodeId
import play.api.libs.json._

import java.io.InputStream
import javax.inject.Singleton
import scala.util.matching.Regex
import scala.util.{Try, Using}

@Singleton
class Functions extends LazyLogging {

  val rr: Try[List[FunctionNode]] = Using(getClass.getResourceAsStream("/FunctionList.json")) { is: InputStream =>
    val ImportFunction: JsValue = Json.parse(is)
    val nodes = ImportFunction.as[List[ImportFunction]]
    nodes.map(_.toFunctionNode)
  }


  val functions: List[FunctionNode] = rr.get
  val macro2FunctionMap: Map[NodeId, FunctionNode] = functions
    .map { fn => fn.nodeId -> fn }
    .toMap

  def size: Int = functions.size

  def get(nodeId: NodeId): Option[FunctionNode] = macro2FunctionMap.get(nodeId)

  def header: Header = Header(s"Functions ($size)", "Id", "Description", "Destination")

}

case class ImportFunction(fn:Int, description:String, destination: Option[String] = None){
  def toFunctionNode:FunctionNode =
    try {
      FunctionNode(FunctionNodeId(fn), description, destination.map(NodeId(_)))
    } catch {
      case e:Exception =>
        e.printStackTrace()
        throw e
    }
}

case class FunctionNodes(functions: List[ImportFunction])

object FunctionNode {
  val parser: Regex = """(\d+)\s+(.*)""".r

  def apply(line: String): FunctionNode = {
    val parser(m, d) = line
    new FunctionNode(FunctionNodeId(m.toInt), d)
  }



  implicit val fmtFn: OFormat[ImportFunction] = Json.format[ImportFunction]
}

case class FunctionNode(nodeId: FunctionNodeId, description: String, destination: Option[NodeId] = None) extends RowSource with Node {
  override def toString: String = s"function: $nodeId description: $description"

  override def toRow: Row = Row(nodeId.toCell, description, destination)
}


case class FunctionNodeId(override val number: Int) extends NodeId  {
  override val prefix: Char = 'f'
  override val cssClass: String = "FunctionNode"

  val callMacro: Option[MacroNodeId] = Option.when(number >= 900) {
    MacroNodeId(number - 900)
  }
}

object FunctionNodeId {
  implicit val nodeIdFormat: Format[FunctionNodeId] = new Format[FunctionNodeId] {
    override def reads(json: JsValue): JsResult[FunctionNodeId] = {

      try {
        JsSuccess(NodeId(json.as[String]).asInstanceOf[FunctionNodeId])
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(nodeId: FunctionNodeId): JsValue = {
      JsString(nodeId.toString)
    }
  }

}