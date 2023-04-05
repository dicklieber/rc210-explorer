package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.{Memory, Slice}

trait FieldDefinition extends RenderMetadata {
  def tooltip: String = ""

  val fieldName: String
  val kind: KeyKind
  val template: String = ""
}

/**
 * A [[SimpleField]] produces one RC-210 command as opposed to a complex field like [[net.wa9nnn.rc210.data.schedules.Schedule]] that may produce multiple commands.
 * And generally will be an HTML form itself to edit.
 *
 * @param offset         where in [[Memory]] this comes from.
 * @param fieldName      as shown to users.
 * @param kind           e.g. [[net.wa9nnn.rc210.key.KeyFactory.MacroKey]] or [[net.wa9nnn.rc210.key.KeyFactory.LogicAlarmKey]]
 * @param template       used to generate the rc-210 command.
 * @param fieldExtractor that knows how to get this from the [[net.wa9nnn.rc210.serial.MemoryBuffer]]
 * @param tooltip        for this field.
 * @param units          suffix for <input>
 * @param max            used by the extractor. e.g. max DtMF digits or max number.
 */
case class SimpleField(offset: Int,
                       fieldName: String,
                       kind: KeyKind,
                       override val template: String,
                       fieldExtractor: FieldExtractor[_],
                       override val tooltip: String = "",
                       override val units: String = "",
                       min:Int = 1,
                       max: Int = 255,
                      ) extends FieldDefinition {
  def extract(iterator: Iterator[Int]): FieldValue = {
    fieldExtractor.extract(iterator, this)
  }

  def fieldKey(number: Int): FieldKey = {
    new FieldKey(fieldName, KeyFactory(kind, number))
  }

  def units(u: String): SimpleField = copy(units = u)

  def max(max: Int): SimpleField = copy(max = max)
  def min(max: Int): SimpleField = copy(min = min)
  def tooltip(tooltip: String): SimpleField = copy(tooltip = tooltip)
}




