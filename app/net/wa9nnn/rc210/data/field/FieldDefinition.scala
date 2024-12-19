package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Form
import play.api.libs.json.JsValue

import scala.util.Try

trait FieldDefinition extends LazyLogging:
  def tooltip: String = ""

  def fieldName: String
  val keyKind: KeyKind
  val template: String = ""
  val units: String = ""

  def positions: Seq[FieldOffset]




abstract class SimpleExtractor extends FieldExtractor :
  def extractFromInts(iterator: Iterator[Int], fieldDefinition: FieldDefinitionSimple): FieldValue


trait FieldExtractor :

  def parse(jsValue: JsValue): FieldValue


/**
 * where in the memory image
 *
 * @param offset          where in [[Memory]].
 * @param fieldDefinition details.
 * @param field           a name.
 */
case class FieldOffset(offset: Int, fieldDefinition: FieldDefinition, field: Option[String] = None)

object FieldOffset {
  def apply(offset: Int, fieldDefinition: FieldDefinition, field: String) = new FieldOffset(offset = offset, fieldDefinition = fieldDefinition, field = Option(field))
}