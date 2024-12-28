package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.clock.ClockNode
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.FormData
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.data.Form
import play.api.libs.json.{Format, JsResult, JsValue, Reads}

import scala.util.Try

/**
 * Knows how to:
 * - extract current values from [[Memory]] (data from RC-210).
 * - provides Play Json Format [[fmt]] to read or write JSON.
 * - instantiate a [[FieldValue]] from an HTML form. [[fromForm()]]
 * - provides a [[KeyMetadata]] to help create [[Key]]s for 
 *
 * @tparam T what this produces.
 */
trait FieldDef[T <: FieldValue] extends LazyLogging with TemplateSource:
  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  def extract(memory: Memory): Seq[FieldEntry]

  def tooltip: String = ""

  def fieldName: String
  val keyMetadata: KeyMetadata
  val template: String = ""
  val units: String = ""

  def positions: Seq[FieldOffset]

  val fmt: Format[T]

  def fromForm(formData: FormData):Seq[UpdateCandidate]

//abstract class SimpleExtractor[T<:FieldValue] extends FieldExtractor :
//  val fmt: Format[FieldValue]
//
//  def extractFromInts(iterator: Iterator[Int], fieldDefinition: FieldDefSimple): FieldValue
//
//  def update(formFieldValue: String): FieldValueSimple
//



/**
 * where in the memory image
 *
 * @param offset          where in [[Memory]].
 * @param fieldDefinition details.
 * @param field           a name.
 */
case class FieldOffset(offset: Int, fieldDefinition: FieldDef[?], field: Option[String] = None)

object FieldOffset :
  def apply(offset: Int, fieldDefinition: FieldDef[?], field: String) = new FieldOffset(offset = offset, fieldDefinition = fieldDefinition, field = Option(field))
