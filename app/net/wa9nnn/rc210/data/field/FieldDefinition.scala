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

/*
/**
 * A [[FieldDefinitionSimple]] produces one RC-210 command as opposed to a complex rc2input like [[net.wa9nnn.rc210.data.schedules.ScheduleNode]] that may produce multiple commands.
 * And generally will be an HTML form itself to edit.
 *
 * @param offset         where in [[Memory]] this comes from.
 * @param fieldName      as shown to users.
 * @param keyKind
 * @param template       used to generate the rc-210 command.
 * @param fieldExtractor that knows how to get this from the [[net.wa9nnn.rc210.serial.Memory]]
 * @param tooltip        for this rc2input.
 * @param units          suffix for <input>
 * @param max            used by the extractor. e.g. max DtMF digits or max number.
 */
case class FieldDefinitionSimple(offset: Int,
                                 fieldName: String,
                                 val keyKind: KeyKind,
                                 override val template: String,
                                 fieldExtractor: SimpleExtractor,
                                 override val tooltip: String = "",
                                 override val units: String = "",
                                 min: Int = 1,
                                 max: Int = 255,
                      ) extends FieldDefinition with LazyLogging :
  def extractFromInts(iterator: Iterator[Int]): Try[FieldValue] = {
    val tried: Try[FieldValue] = Try {
      fieldExtractor.extractFromInts(iterator, this)
    }
    if (tried.isFailure)
      logger.error(s"Extracting: $this. Ignored!")
    tried
  }

  /**
   * Create an [[Iterator[Int]] over the [[Memory]] starting at an offset.
   *
   * @param memoryBuffer data from RC-210 binary dump.
   */
  def iterator()(implicit memoryBuffer: Memory): Iterator[Int] = {
    if (max > 255)
      memoryBuffer.iterator16At(offset)
    else
      memoryBuffer.iterator8At(offset)
  }

  def fieldKey(number: Int): FieldKey = {
    new FieldKey(Key(keyKind, number), fieldName)
  }

  def units(u: String): FieldDefinitionSimple = copy(units = u)

  def max(max: Int): FieldDefinitionSimple = copy(max = max)

  def min(min: Int): FieldDefinitionSimple = copy(min = min)

  def tooltip(tooltip: String): FieldDefinitionSimple = copy(tooltip = tooltip)


  override def positions: Seq[FieldOffset] = Seq(FieldOffset(offset, this))
*/


trait ComplexFieldDefinition[T <: FieldValueComplex] extends FieldExtractor with FieldDefinition with EditHandler {
  def fieldKey(key: Key): FieldKey = FieldKey(key)

  override def fieldName: String = keyKind.entryName

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  def extract(memory: Memory): Seq[FieldEntry]

  def form: Form[T]

}

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