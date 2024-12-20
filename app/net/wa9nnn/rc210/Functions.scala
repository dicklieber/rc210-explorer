package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json

import java.io.InputStream
import scala.util.*

object Functions extends LazyLogging:

  val functions: Seq[FunctionNode] = Using(getClass.getResourceAsStream("/FunctionList.json")) { (is: InputStream) =>
    Json.parse(is).as[List[FunctionNode]]

  } match {
    case Failure(exception) =>
      //      logger.error("FunctionList.json", exception)
      throw exception
    case Success(value) =>
      value
  }
  private val map: Map[Key, FunctionNode] = functions.map(f => f.key -> f).toMap

  /**
   *
   * @param fkey of interest.
   * @return the [[FunctionNode]]
   */
  def maybeFunctionNode(fkey: Key): Option[FunctionNode] =
    fkey.check(KeyMetadata.Function)
    map.get(fkey)

  def description(fKey: Key): String =
    maybeFunctionNode(fKey)
      .map(_.description)
      .getOrElse("")

  def size: Int = functions.length
