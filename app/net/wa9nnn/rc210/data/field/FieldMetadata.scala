package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.field.UiRender.UiRender
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.serial.Memory

import scala.jdk.CollectionConverters._
import scala.util.Try


case class FieldMetadata(offset: Int,
                         fieldName: String,
                         kind: KeyKind,
                         template: String,
                         extractor: FieldExtractor = FieldExtractors.int16,
                         uiRender: UiRender = UiRender.number,
                         selectOptions: Option[FieldSelect] = None
                     ) {


  def %(uiRender: UiRender): FieldMetadata = {
    copy(uiRender = uiRender)
  }

  def %(selectOptions: FieldSelect): FieldMetadata = {
    copy(selectOptions = Option(selectOptions))
  }

  def %(uiInfo: UiInfo): FieldMetadata = {
    val uiRender = uiInfo.uiRender
    val extractor = uiInfo.fieldExtractor

    copy(uiRender = uiRender, extractor = extractor)
  }

  /**
   * template e.g. "$n*2121$b" becomes "3*21210" or  "*2091$b" becomes "*2091$1"
   *
   * @param fieldValue to render.
   * @return
   */
  def command(fieldValue: FieldValue): String = {
    assert(fieldValue.dirty, "No candidate for command!*")
    val sValue = fieldValue.candidate.getOrElse(throw new IllegalStateException("No candidate to send!"))
    val bool: String = if (sValue == "true") "1"
    else
      "0"
    import org.apache.commons.text.StringSubstitutor
    lazy val map: Map[String, String] = Seq(
      "v" -> sValue,
      "b" -> bool,
      "n" -> fieldValue.fieldKey.key.number.toString
    ).toMap
    val substitutor = new StringSubstitutor(map.asJava, "$", "")

    //todo color token and replacement parts <span> s
    substitutor.replace(template)
  }

  def validate(): Unit = {

  }
}