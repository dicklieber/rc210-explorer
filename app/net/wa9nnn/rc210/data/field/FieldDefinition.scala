package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.JsValue

import java.text.FieldPosition
import scala.util.Try

trait FieldDefinition extends LazyLogging:
  def parse(jsValue: JsValue): FieldValue

  def tooltip: String = ""

  val fieldName: String
  val kind: KeyKind
  val template: String = ""
  val units: String = ""

  def positions: Seq[FieldOffset]


/**
 * A [[SimpleField]] produces one RC-210 command as opposed to a complex rc2input like [[net.wa9nnn.rc210.data.schedules.Schedule]] that may produce multiple commands.
 * And generally will be an HTML form itself to edit.
 *
 * @param offset         where in [[Memory]] this comes from.
 * @param fieldName      as shown to users.
 * @param kind           
 * @param template       used to generate the rc-210 command.
 * @param fieldExtractor that knows how to get this from the [[net.wa9nnn.rc210.serial.Memory]]
 * @param tooltip        for this rc2input.
 * @param units          suffix for <input>
 * @param max            used by the extractor. e.g. max DtMF digits or max number.
 */
case class SimpleField(offset: Int,
                       fieldName: String,
                       kind: KeyKind,
                       override val template: String,
                       fieldExtractor: SimpleExtractor,
                       override val tooltip: String = "",
                       override val units: String = "",
                       min: Int = 1,
                       max: Int = 255,
                      ) extends FieldDefinition with LazyLogging {
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
    new FieldKey(fieldName, Key(kind, number))
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

//trait FieldFormParseable[K <: Key, T <: ComplexFieldValue[K]]:
//  def parseFormFields[T <: ComplexFieldValue[K]](using valuesMap: Map[String, String]): T
//
//  def int(name:String):Int = {
//    val x: x.type = summon[Map[String, String]]
//    x
//  }


trait ComplexExtractor(val keyKind: KeyKind) extends FieldExtractor with FieldDefinition  {

  def fieldKey(key: Key): FieldKey = FieldKey(fieldName, key)

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

abstract class SimpleExtractor extends FieldExtractor {
  def extractFromInts(iterator: Iterator[Int], fieldDefinition: SimpleField): FieldValue

}

trait FieldExtractor {

  def parse(jsValue: JsValue): FieldValue

  /**
   * for various things e.g. parser name.
   */
  val name: String

}

/**
 * where in the memory image
 *
 * @param offset
 * @param fieldDefinition
 * @param field
 */
case class FieldOffset(offset: Int, fieldDefinition: FieldDefinition, field: Option[String] = None)

object FieldOffset {
  def apply(offset: Int, fieldDefinition: FieldDefinition, field: String) = new FieldOffset(offset = offset, fieldDefinition = fieldDefinition, field = Option(field))
}