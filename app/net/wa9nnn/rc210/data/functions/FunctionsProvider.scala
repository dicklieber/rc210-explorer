package net.wa9nnn.rc210.data.functions

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.key.KeyFactory.{FunctionKey, Key, MacroKey, MessageKey}
import net.wa9nnn.rc210.key.KeyFormats._
import play.api.libs.json._

import java.io.InputStream
import javax.inject.Singleton
import scala.util.{Failure, Success, Using}

@Singleton
class FunctionsProvider extends LazyLogging {

  val functions: Seq[FunctionNode] = Using(getClass.getResourceAsStream("/FunctionList.json")) { (is: InputStream) =>
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

}

/**
 *
 * @param key         of the function.
 * @param description human readable.
 * @param destination MacroKey or MessageKey
 */
case class FunctionNode(key: FunctionKey, description: String, destination: Option[Key]) extends Ordered[FunctionNode] with RowSource {
  destination foreach (destKey =>
    assert(destKey.isInstanceOf[MacroKey] || destKey.isInstanceOf[MessageKey], s"destination must be MacroKey or MessageKey! But got: $key")
    )

  override def compare(that: FunctionNode): Int = description compareTo that.description

  override def toRow: Row = {
    Row(key.toCell, description, destination)
  }

  override def toString: String = s"$description (${key.number})"
}


object FunctionNode {
  def header(count: Int): Header = Header(s"Functions ($count)", "Key", "Description")
}





