package net.wa9nnn.rc210.data

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.bubble.NodeId
import net.wa9nnn.rc210.model.Node
import net.wa9nnn.rc210.model.macros.MacroNodeId

import javax.inject.Singleton
import scala.io.BufferedSource
import scala.util.matching.Regex
import scala.util.{Try, Using}

@Singleton
class Functions {


  val functions: Try[Iterator[(NodeId, Function)]] =
    Using(new BufferedSource(getClass.getResourceAsStream("/FunctionList.txt"))) { bs: BufferedSource =>
      bs.getLines()
        .map { line =>
          val f = Function(line)
          f.nodeId -> f
        }
    }

  val ordered: Seq[(NodeId, Function)] = functions.get.toSeq
  val macro2FunctionMap: Map[NodeId, Function] = functions.get.toMap

  def size: Int = ordered.size

  def get(nodeId: NodeId): Option[Function] = macro2FunctionMap.get(nodeId)

  def header: Header = Header(s"Functions ($size)", "Id", "Destination", "Description")
}

case class Function(nodeId: FunctionNodeId, description: String) extends RowSource with Node {
  override def toString: String = s"function: $nodeId description: $description"

  override def toRow: Row = Row(nodeId.toString, nodeId.callMacro.getOrElse("-"), description)

  /**
   * What this node can invoke.
   */
  override val outGoing: IterableOnce[NodeId] = nodeId.callMacro
}

object Function {
  val parser: Regex = """(\d+)\s+(.*)""".r

  def apply(line: String): Function = {
    val parser(m, d) = line
    new Function(FunctionNodeId(m.toInt), d)
  }
}

case class FunctionNodeId(override val number: Int) extends NodeId('f', number) {
  val callMacro: Option[NodeId] = Option.when(number >= 900) {
    MacroNodeId(number - 900)
  }
}