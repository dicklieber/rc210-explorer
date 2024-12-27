package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.Functions.{initFunctions, maybeFunctionNode}
import play.api.libs.json.Json

import java.io.InputStream
import scala.io.BufferedSource
import scala.util.*
import com.github.tototoshi.csv.*

import scala.util.matching.Regex

class Functions extends LazyLogging:
  private val inputStream: InputStream = getClass.getResourceAsStream("/FunctionList.csv")
  private val functionNodes: Seq[FunctionNode] = for
    is <- Option(inputStream).toSeq
    bs = BufferedSource(is)
    r: CSVReader = CSVReader.open(bs)
    line <- r.all()
    if line.head != "number"
  yield
    val key = Key(KeyMetadata.Function, line.head.toInt)
    val description = line(1)
    if line.last.isEmpty then
      SimpleFunctionNode(key, description)
    else
      line(2) match
        case Functions.destParser(sMetadata, sNumber)=>
          val metadata = KeyMetadata.withName(sMetadata)
          val destKey = Key(metadata, sNumber.toInt)
          TriggerFunctionNode(key, description, destKey)
  inputStream.close()

  initFunctions(functionNodes)

object Functions:
  val destParser: Regex = """(\D*)(\d+)$""".r
  /**
   * set by [[Functions]] above or by unit tests.
   *
   * @param functions
   */
  def initFunctions(functions: Seq[FunctionNode]): Unit =
    _functions = functions
    _map = functions.map(f => f.key -> f).toMap

  private var _functions: Seq[FunctionNode] = Seq.empty
  private var _map: Map[Key, FunctionNode] = Map.empty

  def functions: Seq[FunctionNode] = _functions

  def description(fKey: Key): String =
    maybeFunctionNode(fKey)
      .map(_.description)
      .getOrElse("")

  def maybeFunctionNode(fkey: Key): Option[FunctionNode] =
    _map.get(fkey)
  def size: Int = _functions.size

