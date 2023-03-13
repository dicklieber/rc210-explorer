package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.key.{KeyFactory, KeyKindEnum}
import net.wa9nnn.rc210.key.KeyKindEnum.KeyKind
import net.wa9nnn.rc210.serial.Memory
import play.twirl.api.Html


case class FD(offset: Int,
              fieldName: String,
              kind: KeyKind,
              template: String,
              uiInfo: UiInfo = UiInfo.default
             ) extends FieldMetadata {
  def extract(start: Int)(implicit memory: Memory): FieldContents = {
    uiInfo.fieldExtractor(memory, start)
  }


  def fieldKey(number: Int): FieldKey = {
    new FieldKey(fieldName, KeyFactory(kind, number))
  }

  def validate(): Unit = {
  }

  override def fieldHtml(fieldKey: FieldKey, fieldContents: FieldContents)(implicit namedSource: NamedSource): String =
    fieldContents.toHtmlField(fieldKey)

  override def prompt: String = uiInfo.prompt
}


trait FieldMetadata {
  def prompt: String

  val fieldName: String

  val kind: KeyKind

  def fieldHtml(fieldKey: FieldKey, fieldContents: FieldContents)(implicit namedSource: NamedSource): String

}


