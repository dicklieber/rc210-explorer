package net.wa9nnn.rc210.data.macros

import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.command.{FunctionKey, MacroKey}
import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.model.Node
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import play.api.libs.json.{Json, OFormat}

import java.util.concurrent.atomic.AtomicInteger

case class Macro(key: MacroKey, dtmf: Dtmf, functions: Seq[FunctionKey]) extends Node {

  override def toRow: Row = {
    val sFunctionString = functions
      .map { fk =>
        fk.index
      }.mkString(" ")
    Row(key.toCell, dtmf, sFunctionString)
      .withId(key.toString)
  }
}

object Macro {
  def header(count:Int): Header = Header(s"Macros ($count)", "Key", "DTMF", "Functions")

  implicit val fmtMacro: OFormat[Macro] = Json.format[Macro]
}

object MacroExtractor {


  def apply(memory: Memory): Seq[Macro] = {
    val dtmfMacroMap: DtmfMacros = DtmfMacroExractor(memory)
    val mai = new AtomicInteger(1)

    /**
     *
     * @param macroSlicePos the functions in macro.
     * @param memory        from the RC-210
     * @param bytesPerMacro         how many slots in a macro.
     * @return the macros with functions, dtmf code
     */
    def macroBuilder(macroSlicePos: SlicePos, memory: Memory, bytesPerMacro: Int) = {
      val macrosSlice = memory(macroSlicePos)
      val f: Seq[Macro] = macrosSlice.data
        .grouped(bytesPerMacro)
        .map { bytes =>
          val functions: Seq[FunctionKey] = bytes.takeWhile(_ != 0).map(fn => FunctionKey(fn))
          val macroKey = MacroKey(mai.getAndIncrement())
          Macro(macroKey, dtmfMacroMap(macroKey), functions)
        }.toSeq

      f
    }

    val longMacros: Seq[Macro] = macroBuilder(SlicePos("//Macro - 1985-2624"), memory, 16)
    val shortMacros: Seq[Macro] = macroBuilder(SlicePos("//ShortMacro - 2825-3174"), memory, 7)
    //    val extendedMacros: Seq[Macro] = macroBuilder(SlicePos("//Extended Macros 1 - 390 (91 - 105)"), memory, 20) //todo rtc
    val r: Seq[Macro] = longMacros.concat(shortMacros)
    r


  }


}

case class DtmfMacro(value: String, macroKey: MacroKey)
