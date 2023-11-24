package net.wa9nnn.rc210.data.functions

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.libs.json.*

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
  def apply(fkey: Key): Option[FunctionNode] =
    fkey.check(KeyKind.functionKey)
    map.get(fkey)

  def size: Int = functions.length


//  lazy val invokedMacros: Seq[Key] = for {
//    function <- functions
//    destKey <- function.destination
//    if destKey.isInstanceOf[Key]
//  } yield {
//    destKey.asInstanceOf[Key]
//  }

}

/**
 *
 * @param key         of the function.
 * @param description human readable.
 * @param destination Key or MessageKey
 */
case class FunctionNode(key: Key, description: String, destination: Option[Key]) extends Ordered[FunctionNode] with RowSource {
  destination foreach (destKey =>
    assert(destKey.keyKind == KeyKind.macroKey || destKey.keyKind == KeyKind.messageKey, s"destination must be Key or MessageKey! But got: $key")
    )

  override def compare(that: FunctionNode): Int = description compareTo that.description

  override def toRow: Row = {
    Row(key.toCell, description, destination)
  }

  override def toString: String = s"$description (${key.rc210Value})"
}

object FunctionNode {
  def header(count: Int): Header = Header(s"Functions ($count)", "Key", "Description")

  implicit val fmtFunction: Format[FunctionNode] = new Format[FunctionNode] {
    override def reads(json: JsValue): JsResult[FunctionNode] = {

      try {
        val key: Key = (json \ "key").as[Key]

        val sdesc: String = (json \ "description").as[String]
        val dest: Option[Key] = (json \ "destination").asOpt[Key]

        val f = FunctionNode(key, sdesc, dest)
        JsSuccess(f)
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(key: FunctionNode): JsValue = {
      JsString(key.toString)
    }
  }

}





