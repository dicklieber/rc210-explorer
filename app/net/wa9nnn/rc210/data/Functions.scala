package net.wa9nnn.rc210.data

import com.wa9nnn.util.tableui.{Header, Row, RowSource}

import javax.inject.Singleton
import scala.io.BufferedSource
import scala.util.matching.Regex
import scala.util.{Try, Using}

@Singleton
class Functions {

  val functions: Try[Iterator[(Int, Function)]] =
    Using(new BufferedSource(getClass.getResourceAsStream("/FunctionList.txt"))) { bs: BufferedSource =>
      bs.getLines()
        .map { line =>
          val f = Function(line)
          f.id -> f
        }
    }

  val ordered: Seq[(Int, Function)] =  functions.get.toSeq
  val macro2FunctionMap: Map[Int, Function] = functions.get.toMap

  def get(macroNUmber: Int): Option[Function] = macro2FunctionMap.get(macroNUmber)
}

case class Function(id: Int, description: String) extends RowSource {
  override def toString: String = s"function: $id description: $description"

  val callMacro: Option[Int] = Option.when(id >= 900) {
    id - 900
  }

  override def toRow: Row = Row(id.toString, callMacro.getOrElse("-"), description)
}

object Function {
  val header: Header = Header("Functions", "Id", "Destination", "Description")
  val parser: Regex = """(\d+)\s+(.*)""".r

  def apply(line: String): Function = {
    val parser(m, d) = line
    new Function(m.toInt, d)
  }
}
