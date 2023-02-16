package net.wa9nnn.rc210.data.macros

import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.data.Formats._
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.model.Node
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import net.wa9nnn.rc210.{FunctionKey, MacroKey}

import java.util.concurrent.atomic.AtomicInteger

case class MacroNode(key: MacroKey, dtmf: Dtmf, functions: Seq[FunctionKey]) extends Node {

  override def toRow: Row = {
    val sFunctionString = functions
      .map { fk =>
        fk.index
      }.mkString(" ")
    Row(key.toCell, dtmf, sFunctionString)
      .withId(key.toString)
  }

  def table()(implicit functionsProvider: FunctionsProvider): Table = {
    val rows = for {
      functionKey <- functions
      f <- functionsProvider(functionKey)
    } yield {
      f.toRow
    }

    Table(Seq.empty, rows)
  }
}

object MacroNode {
  def header(count: Int): Header = Header(s"Macros ($count)", "Key", "DTMF", "Functions")

}

object MacroExtractor {


  def apply(memory: Memory): Seq[MacroNode] = {
    val dtmfMacroMap: DtmfMacros = DtmfMacroExractor(memory)
    val mai = new AtomicInteger(1)

    /**
     *
     * @param macroSlicePos         the functions in macro.
     * @param memory                from the RC-210
     * @param bytesPerMacro         how many slots in a macro.
     * @return the macros with functions, dtmf code
     */
    def macroBuilder(macroSlicePos: SlicePos, memory: Memory, bytesPerMacro: Int) = {
      val macrosSlice = memory(macroSlicePos)
      val f: Seq[MacroNode] = macrosSlice.data
        .grouped(bytesPerMacro)
        .map { bytes =>
          val functions: Seq[FunctionKey] = bytes.takeWhile(_ != 0).map(fn => FunctionKey(fn))
          val macroKey = MacroKey(mai.getAndIncrement())
          MacroNode(macroKey, dtmfMacroMap(macroKey), functions)
        }.toSeq

      f
    }

    val longMacros: Seq[MacroNode] = macroBuilder(SlicePos("//Macro - 1985-2624"), memory, 16)
    val shortMacros: Seq[MacroNode] = macroBuilder(SlicePos("//ShortMacro - 2825-3174"), memory, 7)
    //    val extendedMacros: Seq[Macro] = macroBuilder(SlicePos("//Extended Macros 1 - 390 (91 - 105)"), memory, 20) //todo rtc
    val r: Seq[MacroNode] = longMacros.concat(shortMacros)
    r


  }


}

case class DtmfMacro(value: String, macroKey: MacroKey)
