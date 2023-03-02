package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.{KeyFormats, KeyKind}
import net.wa9nnn.rc210.serial.Memory


case class FieldMetadata(offset: Int,
                         fieldName: String,
                         kind: KeyKind,
                         template: String,

                         uiInfo: UiInfo = UiInfo.default,

                         //                         extractor: FieldExtractor = FieldExtractors.int16,
                         //                         uiRender: UiRender = UiRender.number,
                         selectOptions: Option[FieldSelect] = None
                        ) {
  def extract(memory: Memory, start: Int) = uiInfo.fieldExtractor(memory, start)


  def %(uiInfo: UiInfo): FieldMetadata = {
    copy(uiInfo = uiInfo)
  }


  def fieldKey(number: Int): FieldKey = {

    new FieldKey(fieldName, KeyFormats.buildKey(kind, number))
  }

  def validate(): Unit = {

  }
}