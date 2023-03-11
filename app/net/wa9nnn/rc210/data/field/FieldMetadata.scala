package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.key.KeyKindEnum.KeyKind
import net.wa9nnn.rc210.serial.Memory


case class FieldMetadata(offset: Int,
                         fieldName: String,
                         kind: KeyKind,
                         template: String,
                         uiInfo: UiInfo = UiInfo.default,
                         selectOptions: Option[UiSelect] = None
                        ) {
  def extract(memory: Memory, start: Int): FieldContents = uiInfo.fieldExtractor(memory, start)


  def %(uiInfo: UiInfo): FieldMetadata = {
    copy(uiInfo = uiInfo)
  }


  def fieldKey(number: Int): FieldKey = {

    new FieldKey(fieldName, KeyFactory(kind, number))
  }

  def validate(): Unit = {

  }
}