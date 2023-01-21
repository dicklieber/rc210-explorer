package net.wa9nnn.rc210.model.macros

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.DatFile
import net.wa9nnn.rc210.bubble.NodeId
import net.wa9nnn.rc210.model.{DataItem, Node}

import scala.util.Try

case class Macro(macroNumber: Int, dtmf: Option[Int], functions: List[Int]) extends RowSource with Node {
  override def toRow: Row = {
    Row(macroNumber.toString, dtmf.map(_.toString).getOrElse(" "), functions.mkString(" "))
  }

  override val nodeId: NodeId = NodeId('m', macroNumber)
}

object Macro extends LazyLogging {
  val header: Header = Header("Macros", "Macro", "DTMF", "Functions")

  private def buildMacro(macroNumber: Int, items: Seq[DataItem]): Try[Macro] = {
    {
      val valueMap: Map[String, DataItem] = items.map { dataItem =>
        dataItem.name -> dataItem
      }.toMap

      Try {
        val dtmf: String = valueMap("MacroCode").value
        val xx: List[Int] = valueMap("Macro").value.split(" ").toList.map(_.toInt)
        new Macro(macroNumber,
          Option.when(dtmf.nonEmpty)(dtmf.toInt),
          xx
        )
      }
    }
  }

  def extractMacros(datFile: DatFile): Seq[Macro] = {
    for {
      pair: (Option[Int], Seq[DataItem]) <- datFile.section("Macros").dataItems
        .groupBy(_.maybeInt)
        .toSeq
        .sortBy(_._1)
      if pair._1.isDefined // has number
      schedule <- Macro.buildMacro(pair._1.get, pair._2).toOption
    } yield {
      schedule
    }
  }

}
