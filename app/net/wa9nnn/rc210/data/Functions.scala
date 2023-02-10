package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.command.{FunctionKey, MacroKey}
import net.wa9nnn.rc210.model.Node
import play.api.libs.json._

import java.io.InputStream
import javax.inject.Singleton
import scala.util.matching.Regex
import scala.util.{Try, Using}

@Singleton
class Functions extends LazyLogging {

  val rr: Try[List[FunctionNode]] = Using(getClass.getResourceAsStream("/FunctionList.json")) { is: InputStream =>
    val ImportFunction: JsValue = Json.parse(is)

    throw new NotImplementedError() //todo
//    val nodes = ImportFunction.as[List[ImportFunction]]
//    nodes.map(_.toFunctionNode)
  }


//  val functions: List[FunctionNode] = rr.get
//  val macro2FunctionMap: Map[MacroKey, FunctionNode] = functions
//    .map { fn => fn.key -> fn }
//    .toMap

  def size: Int = -1//todofunctions.size

  def get(nodeId: MacroKey): Option[FunctionNode] = throw new NotImplementedError() //todomacro2FunctionMap.get(nodeId)

  def header: Header = Header(s"Functions ($size)", "Id", "Description", "Destination")

}

//case class ImportFunction(fn:Int, description:String, destination: Option[String] = None){
//  def toFunctionNode:FunctionNode =
//    try {
//      FunctionNode(FunctionNodeId(fn), description, destination.map(NodeId(_)))
//    } catch {
//      case e:Exception =>
//        e.printStackTrace()
//        throw e
//    }
//}

//case class FunctionNodes(functions: List[FunctionNode])

object FunctionNode {
  val parser: Regex = """(\d+)\s+(.*)""".r

//  def apply(line: String): FunctionNode = {
//    val parser(m, d) = line
//    new FunctionNode(FunctionNodeId(m.toInt), d)
//  }



//  implicit val fmtFn: OFormat[ImportFunction] = Json.format[ImportFunction]
}

case class FunctionNode(key: FunctionKey, description: String, destination: Option[MacroKey] = None) extends RowSource with Node {
  override def toString: String = s"key: $key description: $description"

  override def toRow: Row = Row(key.toCell, description, destination)
}




