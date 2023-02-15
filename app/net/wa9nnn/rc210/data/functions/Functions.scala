package net.wa9nnn.rc210.data.functions

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.data.functions.Function._
import net.wa9nnn.rc210.{FunctionKey, Key, MacroKey, MessageMacroKey}
import play.api.libs.json._

import java.io.InputStream
import javax.inject.Singleton
import scala.util.{Failure, Success, Using}
import net.wa9nnn.rc210.data.Formats._
@Singleton
class Functions extends LazyLogging {

  val functions: Seq[Function] = Using(getClass.getResourceAsStream("/FunctionList.json")) { is: InputStream =>
    Json.parse(is).as[List[Function]]

  } match {
    case Failure(exception) =>
      //      logger.error("FunctionList.json", exception)
      throw exception
    case Success(value) =>
      value
  }

  val byDescription: Seq[Function] = functions.sorted
  private val map = functions.map(f => f.key -> f).toMap

  /**
   *
   * @param fkey of interest.
   * @return the [[Function]]
   */
  def apply(fkey: FunctionKey): Option[Function] = map.get(fkey)

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

case class Function(key: FunctionKey, description: String, destination: Option[Key]) extends Ordered[Function] with RowSource {
  override def toRow: Row = Row(key.toCell, description, destination)

  override def compare(that: Function): Int = description compareTo that.description
}


object Function {
  def header(count:Int): Header = Header(s"Functions ($count)", "Key", "Description")
}





