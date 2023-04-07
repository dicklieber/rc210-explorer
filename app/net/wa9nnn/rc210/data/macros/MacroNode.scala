package net.wa9nnn.rc210.data.macros

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.key.KeyFactory.{FunctionKey, MacroKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.MemoryBuffer
import play.api.libs.json.{Format, JsValue, Json}

import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @param key       unique id for this macro.
 * @param functions that this macro invokes.
 * @param dtmf      that can invoke this macro.
 */
case class MacroNode(override val key: MacroKey, functions: Seq[FunctionKey], dtmf: Option[Dtmf] = None) extends ComplexFieldValue[MacroKey] with TriggerNode {

  def enabled: Boolean = functions.nonEmpty


  //  override val commandStringValue: String = "*4002 10 * 162 * 187 * 122 * 347" // todo


  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  override def macroToRun: MacroKey = ???

  override def triggerEnabled: Boolean = ???

  override def triggerDescription: String = ???

  override def toRow: Row = {
    throw new NotImplementedError() //todo
  }

  override val fieldName: String = "Macro"

  override def display: String = functions.map(_.toString).mkString(" ")

  override def toRow()(implicit namedSource: NamedSource): Row = {
    throw new NotImplementedError() //todo
  }
  override def toJsonValue: JsValue = Json.toJson(this)

}

object MacroNode extends LazyLogging with ComplexExtractor  {
  def header(count: Int): Header = Header(s"Macros ($count)", "Key", "Functions")

  override def extract(memoryBuffer: MemoryBuffer): Seq[FieldEntry] = {

    val dtmfMap: DtmfMacros = DtmfMacroExtractor(memoryBuffer)
    val mai = new AtomicInteger(1)

    def macroBuilder(offset: Int, chunkLength: Int, nChunks:Int) = {
      memoryBuffer.chunks(offset, chunkLength, nChunks)
        .map { chunk: Array[Int] =>
          val key: MacroKey = KeyFactory(KeyKind.macroKey, mai.getAndIncrement())
          val f: Array[FunctionKey] = chunk.takeWhile(_ != 0)
            .map(number => KeyFactory.functionKey(number))
          MacroNode(key, f.toIndexedSeq, dtmfMap(key))

        }
    }

    val macros: Seq[MacroNode] = macroBuilder(1985, 16, 40) //SlicePos("//Macro - 1985-2624"), memory, 16)
      .concat(macroBuilder(2825, 7, 50)) // SlicePos("//ShortMacro - 2825-3174"), memory, 7))

    val r: Seq[FieldEntry] = macros.map { m: MacroNode =>
      FieldEntry(fieldDefinition, m.fieldKey, m)
    }
    r
  }
  //*4002 10 * 162 * 187 * 122 * 347
  //*2050 xxx yyyyyyyy where xxx is the 3 digit Macro number (001-105) and yyyyyyyy is the DTMF code up to 8 digits


import net.wa9nnn.rc210.key.KeyFormats._
  implicit val fmtMacroNode: Format[MacroNode] = Json.format[MacroNode]
  override def jsonToField(jsValue: JsValue): FieldValue = {
    jsValue.as[MacroNode]
  }

  override val name: String = "MacroNode"
}


