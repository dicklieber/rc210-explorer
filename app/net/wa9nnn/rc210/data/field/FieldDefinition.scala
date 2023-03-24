package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.{Memory, Slice}

trait FieldDefinition {
  def prompt: String = ""
  val fieldName: String
  val kind: KeyKind
  val uiInfo:UiInfo = UiInfo.default
  val template:String = ""
}

/**
 * A [[SimpleField]] produces one RC-210 command as opposed to a complex field like [[net.wa9nnn.rc210.data.schedules.Schedule]] that may produce multiple commands.
 * And generally will be an HTML form itself to edit.
 * @param offset where in [[Memory]] this comes from.
 * @param fieldName as shown to users.
 * @param kind e.g. [[net.wa9nnn.rc210.key.MacroKey]] or [[net.wa9nnn.rc210.key.AlarmKey]]
 * @param template used to generate the rc-210 command.
 * @param uiInfo about how to parse and render this sort of field.
 */
case class SimpleField(offset: Int,
                       fieldName: String,
                       kind: KeyKind,
                       override val template: String,
                       override val uiInfo: UiInfo  = UiInfo.default,
             ) extends FieldDefinition {
  def extract(start: Int)(implicit memory: Memory): (FieldContents, Slice) = {
    uiInfo.fieldExtractor(memory, start)
  }


  def fieldKey(number: Int): FieldKey = {
    new FieldKey(fieldName, KeyFactory(kind, number))
  }

  def validate(): Unit = {
  }

  override def prompt: String = uiInfo.prompt
}




