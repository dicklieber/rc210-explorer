package net.wa9nnn.rc210.data.macros

import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.command.{FunctionKey, Key, MacroKey}
import net.wa9nnn.rc210.model.Node
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}
import play.api.libs.json.{Json, OFormat}

import java.util.concurrent.atomic.AtomicInteger

case class Macro(key: MacroKey, functions: Seq[FunctionKey]) extends Node {

  override def toRow: Row = {
    val sFunctionString = functions
      .map { fk =>
        fk.index
      }.mkString(" ")
    Row(key.toCell, sFunctionString)
  }
}

object Macro {

  import net.wa9nnn.rc210.command.Key._

  implicit val fmtMacro: OFormat[Macro] = Json.format[Macro]
  /* Macro 1
  long
  1985,165
  1986,85
  1987,27
  1988,60
  1989,196
  1990,0
  1991,255
  1992,255
  1993,255
  1994,255
  1995,255
  1996,255
  1997,255
  1998,255
  1999,255
  2000,255

   */


  def apply(memory: Memory): Seq[Macro] = {
    val m = new AtomicInteger(1)
    //    LongMacros(memory),
    val longMacros: Seq[Macro] = macroBuilder(SlicePos("//Macro - 1985-2624"), memory, 16, m)
    val shortMacros: Seq[Macro] = macroBuilder(SlicePos("//ShortMacro - 2825-3174"), memory, 7, m)
    //    val extendedMacros: Seq[Macro] = macroBuilder(SlicePos("//Extended Macros 1 - 390 (91 - 105)"), memory, 20) //todo rtc
    val r: Seq[Macro] = longMacros
      .concat(shortMacros)
    r
  }

  def macroBuilder(slicePos: SlicePos, memory: Memory, slots: Int, m: AtomicInteger): Seq[Macro] = {
    val macrosSlice = memory(slicePos)
    val f: Seq[Macro] = macrosSlice.data
      .grouped(slots)
      .map { bytes =>
        val functions: Seq[FunctionKey] = bytes.takeWhile(_ != 0).map(fn => FunctionKey(fn))
        Macro(MacroKey(m.getAndIncrement()), functions)
      }.toSeq

    f
  }

  val header: Header = Header("Macros", "Key", "Functions")
}

case class DtmfMacro(value: String, macroKey: MacroKey)
