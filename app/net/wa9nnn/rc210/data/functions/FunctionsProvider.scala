package net.wa9nnn.rc210.data.functions

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, RowSource}
import net.wa9nnn.rc210.{FunctionKey, Key, MacroKey, MessageMacroKey}
import play.api.libs.json._
import net.wa9nnn.rc210.data.Formats._
import net.wa9nnn.rc210.data.Rc210Data

import java.io.InputStream
import javax.inject.Singleton
import scala.util.{Failure, Success, Using}

@Singleton
class FunctionsProvider extends LazyLogging {

  val functions: Seq[FunctionNode] = Using(getClass.getResourceAsStream("/FunctionList.json")) { is: InputStream =>
    Json.parse(is).as[List[FunctionNode]]

  } match {
    case Failure(exception) =>
      //      logger.error("FunctionList.json", exception)
      throw exception
    case Success(value) =>
      value
  }

  val byDescription: Seq[FunctionNode] = functions.sorted
  private val map = functions.map(f => f.key -> f).toMap

  /**
   *
   * @param fkey of interest.
   * @return the [[FunctionNode]]
   */
  def apply(fkey: FunctionKey): Option[FunctionNode] = map.get(fkey)

  def size: Int = functions.length


  lazy val invokedMacros: Seq[MacroKey] = for {
    function <- functions
    destKey <- function.destination
    if destKey.isInstanceOf[MacroKey]
  } yield {
    destKey.asInstanceOf[MacroKey]
  }
  lazy val invokedMessageMacros: Seq[MessageMacroKey] = for {
    function <- functions
    destKey <- function.destination
    if destKey.isInstanceOf[MessageMacroKey]
  } yield {
    destKey.asInstanceOf[MessageMacroKey]
  }

}

case class FunctionNode(key: FunctionKey, description: String, destination: Option[Key]) extends Ordered[FunctionNode] with RowSource {

  def toRowMacroBlockRow()(implicit rc210Data: Rc210Data): Row = {

    val cell = Cell(description).withToolTip(key.toString)
    val row: Row = new Row(Seq(cell))

    if (destination.nonEmpty)
      row :+ destination.get
    else
      row // we don't want the <td> in this case
  }

  override def compare(that: FunctionNode): Int = description compareTo that.description

  override def toRow: Row = {
    Row(key.toCell, destination, destination)
  }
}


object FunctionNode {
  def header(count: Int): Header = Header(s"Functions ($count)", "Description")
}





