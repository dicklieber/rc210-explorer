package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.JsValue

import java.text.FieldPosition
import scala.util.Try

trait FieldDefinition {
  def parse(jsValue: JsValue): FieldValue


  def tooltip: String = ""

  val fieldName: String
  val kind: KeyKind
  val template: String = ""
  val units: String = ""
  def positions: Seq[FieldOffset]
}

/**
 * A [[SimpleField]] produces one RC-210 command as opposed to a complex field like [[net.wa9nnn.rc210.data.schedules.Schedule]] that may produce multiple commands.
 * And generally will be an HTML form itself to edit.
 *
 * @param offset         where in [[Memory]] this comes from.
 * @param fieldName      as shown to users.
 * @param kind           e.g. [[net.wa9nnn.rc210.key.KeyFactory.MacroKey]] or [[net.wa9nnn.rc210.key.KeyFactory.LogicAlarmKey]]
 * @param template       used to generate the rc-210 command.
 * @param fieldExtractor that knows how to get this from the [[net.wa9nnn.rc210.serial.Memory]]
 * @param tooltip        for this field.
 * @param units          suffix for <input>
 * @param max            used by the extractor. e.g. max DtMF digits or max number.
 */
case class SimpleField(offset: Int,
                       fieldName: String,
                       kind: KeyKind,
                       override val template: String,
                       fieldExtractor: SimpleExtractor[_],
                       override val tooltip: String = "",
                       override val units: String = "",
                       min: Int = 1,
                       max: Int = 255,
                      ) extends FieldDefinition with LazyLogging{
  def extractFromInts(iterator: Iterator[Int]): Try[FieldValue] = {
    val tried: Try[FieldValue] = Try {
      fieldExtractor.extractFromInts(iterator, this)
    }
    if(tried.isFailure)
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
    new FieldKey(fieldName, KeyFactory(kind, number))
  }

  def units(u: String): SimpleField = copy(units = u)

  def max(max: Int): SimpleField = copy(max = max)

  def min(min: Int): SimpleField = copy(min = min)

  def tooltip(tooltip: String): SimpleField = copy(tooltip = tooltip)

  override def parse(json: JsValue): FieldValue = {
    fieldExtractor.parse(json)
  }

  override def positions: Seq[FieldOffset] = Seq(FieldOffset(offset, this))
}

trait ComplexExtractor[K <: Key] extends FieldExtractor with FieldDefinition  with LazyLogging{

  def fieldKey(key:K):FieldKey = FieldKey(fieldName, key)
  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  def extract(memory: Memory): Seq[FieldEntry]

  //   lazy val fieldDefinition: FieldDefinition = {
  //    new FieldDefinition {
  //      override val fieldName: String = name
  //      override val kind: KeyKind = KeyKind.courtesyToneKey
  //
  //      override def parse(jsValue: JsValue): FieldValue
  //    }
  //  }

}

abstract class SimpleExtractor[T] extends FieldExtractor {
  def extractFromInts(iterator: Iterator[Int], fieldDefinition: SimpleField): FieldValue
  def fromForm(name:String)(implicit kv: Map[String, String], key: Key):T =
    throw new NotImplementedError() //todo

  def formValue(name:String)(implicit kv: Map[String, String], key: Key):String = {
    kv(FieldKey(name, key).param)
  }
}

trait FieldExtractor {

  def parse(jsValue: JsValue): FieldValue

  /**
   * for various things e.g. parser name.
   */
  val name: String

}

case class FieldOffset(offset:Int, fieldDefinition: FieldDefinition)