package net.wa9nnn.rc210.data.macros

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.MemoryExtractor
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.key.KeyKindEnum.{KeyKind, macroKey}
import net.wa9nnn.rc210.key.{FunctionKey, KeyKindEnum, MacroKey}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.{Memory, SlicePos}

import java.util.concurrent.atomic.AtomicInteger

case class MacroNode(fieldKey: FieldKey, functions: Seq[FunctionKey]) extends FieldContents  with TriggerNode{
  def enabled: Boolean = functions.nonEmpty


  import net.wa9nnn.rc210.key.KeyFormats._
  import play.api.libs.json._

  override def toJsValue: JsValue = Json.toJson(functions)

  //  override val commandStringValue: String = "*4002 10 * 162 * 187 * 122 * 347" // todo


  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  /**
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   * @param fieldEntry all the metadata.
   * @return html
   */
  override def toHtmlField(fieldEntry: FieldEntry): String = ???

  override def macroToRun: MacroKey = ???

  override def triggerEnabled: Boolean = ???

  override def triggerDescription: String = ???

  override def toRow: Row = ???

  val key = fieldKey.key



}

object MacroNode extends LazyLogging with MemoryExtractor with FieldDefinition {
  def header(count: Int): Header = Header(s"Macros ($count)", "Key",  "Functions")

  override def extract(memory: Memory): Seq[FieldEntry] = {
    val mai = new AtomicInteger(1)

    def macroBuilder(macroSlicePos: SlicePos, memory: Memory, bytesPerMacro: Int) = {
      memory(macroSlicePos).data
        .grouped(bytesPerMacro)
        .map { bytes =>
          val functions: Seq[FunctionKey] = bytes.takeWhile(_ != 0).map(fn => FunctionKey(fn))
          val key: MacroKey = KeyKindEnum.macroKey[MacroKey](mai.getAndIncrement())
          val fieldKey = key.fieldKey("Macro")
          MacroNode(fieldKey, functions)
        }.toSeq
    }

    val macros: Seq[MacroNode] = macroBuilder(SlicePos("//Macro - 1985-2624"), memory, 16)
      .concat(macroBuilder(SlicePos("//ShortMacro - 2825-3174"), memory, 7))

    val r: Seq[FieldEntry] = macros.map { m: MacroNode =>
      FieldEntry(this,m.fieldKey, m )
    }
    r
  }
  //*4002 10 * 162 * 187 * 122 * 347
  //*2050 xxx yyyyyyyy where xxx is the 3 digit Macro number (001-105) and yyyyyyyy is the DTMF code up to 8 digits


  override val fieldName: String = "Macro"
  override val kind: KeyKind = macroKey

  override def prompt: String = ""
//  override def fieldHtml(fieldKey: FieldKey, fieldContents: FieldContents)(implicit namedSource: NamedSource): Html = ???
}


