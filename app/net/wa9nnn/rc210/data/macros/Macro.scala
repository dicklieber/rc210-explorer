package net.wa9nnn.rc210.data.macros

import com.wa9nnn.util.tableui.Row
import net.wa9nnn.rc210.command.{Key, MacroKey}
import net.wa9nnn.rc210.model.Node
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

case class Macro(key: Key) extends Node {

  override def toRow: Row = throw new NotImplementedError() //todo???
}

object Macro {
  def apply(macroKey: MacroKey, slice: Slice): Macro = {

//    macroKey.slots
    throw new NotImplementedError() //todo
  }


  def apply(memory: Memory): Seq[Macro] = {
    //    LongMacros(memory),
    //    ShortMacros(memory),
    //    ExtendedMacros(memory),
    Seq.empty

    //SlicePos("1985-2624")
  }

  def macroBuilder(slicePos: SlicePos, memory: Memory): Seq[Macro] = {

    throw new NotImplementedError() //todo
  }
}

case class DtmfMacro(value: String, macroKey: MacroKey)
