package net.wa9nnn.rc210.data.functions

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.command.{FunctionKey, Key, MacroKey, MessageMacroKey}
import net.wa9nnn.rc210.data.functions.Function._
import play.api.libs.json._

import java.io.InputStream
import javax.inject.Singleton
import scala.util.{Failure, Success, Using}

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
  @throws[NoSuchElementException]("If fkey is not defined.")
  def apply(fkey: FunctionKey): Function = map(fkey)

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

  def header: Header = Header(s"Functions ($size)", "Id", "Description", "Destination")
}

case class Function(key: FunctionKey, description: String, destination: Option[Key]) extends Ordered[Function] with RowSource {
  override def toRow: Row = Row(key.toCell, description, destination)

  override def compare(that: Function): Int = description compareTo that.description
}


object Function {
//  implicit val fmtFunction: Format[Function] = Json.format[Function]
  //todo  I don't understand why the above fails but the below work.

  implicit val fmtFunction: Format[Function] = new Format[Function] {
    override def reads(json: JsValue): JsResult[Function] = {

      try {
        val jsKey: Key = (json \ "key").as[Key]

        val sdesc: String = (json \ "description").as[String]
        val sdest: Option[Key] = (json \ "destination").asOpt[Key]

        val f = Function(jsKey.asInstanceOf[FunctionKey], sdesc, sdest)
        JsSuccess(f)
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(key: Function): JsValue = {
      JsString(key.toString)
    }
  }
}





