package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.clock.ClockNode
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.{ Key, KeyMetadata}
import play.api.data.Form
import play.api.libs.json.{Format, JsResult, JsValue, Reads}

import scala.util.Try

trait FieldDef[T <: FieldValue] extends LazyLogging with TemplateSource:
  def tooltip: String = ""

  def fieldName: String
  val keyMetadata: KeyMetadata
  val template: String = ""
  val units: String = ""

  def positions: Seq[FieldOffset]
  
  
abstract class SimpleExtractor[T<:FieldValueSimple] extends FieldExtractor :
  def extractFromInts(iterator: Iterator[Int], fieldDefinition: FieldDefSimple): FieldValue

  def update(formFieldValue: String): FieldValueSimple
  val fmt: Format[T]


trait FieldExtractor



/**
 * where in the memory image
 *
 * @param offset          where in [[Memory]].
 * @param fieldDefinition details.
 * @param field           a name.
 */
case class FieldOffset(offset: Int, fieldDefinition: FieldDef[?], field: Option[String] = None)

object FieldOffset {
  def apply(offset: Int, fieldDefinition: FieldDef[?], field: String) = new FieldOffset(offset = offset, fieldDefinition = fieldDefinition, field = Option(field))
}