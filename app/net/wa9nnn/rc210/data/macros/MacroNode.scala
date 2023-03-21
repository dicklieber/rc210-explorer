package net.wa9nnn.rc210.data.macros

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.MemoryExtractor
import net.wa9nnn.rc210.data.{Dtmf, FieldKey}
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.key.KeyKindEnum.{KeyKind, macroKey, scheduleKey}
import net.wa9nnn.rc210.key.{FunctionKey, Key, KeyKindEnum, MacroKey}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.{Memory, SlicePos}

import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @param fieldKey  unique id for this macro.
 * @param functions that this kacro oinvokes.
 * @param dtmf      that can invoke this macro.
 */
case class MacroNode(override val key: MacroKey, functions: Seq[FunctionKey], dtmf: Option[Dtmf] = None) extends FieldWithFieldKey[MacroKey] with TriggerNode {

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
  override def toHtmlField(fieldEntry: FieldEntry): String = {
    throw new NotImplementedError() //todo
  }

  override def macroToRun: MacroKey = ???

  override def triggerEnabled: Boolean = ???

  override def triggerDescription: String = ???

  override def toRow: Row = {
    throw new NotImplementedError() //todo
  }

  override val fieldName: String = "Macro"

  override def display: String = functions.map(_.toString).mkString(" ")
}

object MacroNode extends LazyLogging with MemoryExtractor with FieldDefinition {
  def header(count: Int): Header = Header(s"Macros ($count)", "Key", "Functions")

  override def extract(memory: Memory): Seq[FieldEntry] = {

    val dtmfMap: DtmfMacros = DtmfMacroExractor(memory)
    val mai = new AtomicInteger(1)

    def macroBuilder(macroSlicePos: SlicePos, memory: Memory, bytesPerMacro: Int) = {
      memory(macroSlicePos).data
        .grouped(bytesPerMacro)
        .map { bytes =>
          val functions: Seq[FunctionKey] = bytes.takeWhile(_ != 0).map(fn => FunctionKey(fn))
          val key: MacroKey = KeyKindEnum.macroKey[MacroKey](mai.getAndIncrement())
          MacroNode(key, functions, dtmfMap(key))
        }.toSeq
    }

    val macros: Seq[MacroNode] = macroBuilder(SlicePos("//Macro - 1985-2624"), memory, 16)
      .concat(macroBuilder(SlicePos("//ShortMacro - 2825-3174"), memory, 7))

    val r: Seq[FieldEntry] = macros.map { m: MacroNode =>
      FieldEntry(this, m.fieldkey, m)
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


