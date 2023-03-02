package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.key.KeyKind
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


  def %(selectOptions: FieldSelect): FieldMetadata = {
    copy(selectOptions = Option(selectOptions))
  }

  def %(uiInfo: UiInfo): FieldMetadata = {
    copy(uiInfo = uiInfo)
  }


  def validate(): Unit = {

  }
}