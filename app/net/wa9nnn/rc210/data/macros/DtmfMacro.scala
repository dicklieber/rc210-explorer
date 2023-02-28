package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.key.MacroKey
import net.wa9nnn.rc210.serial.{Memory, SlicePos}

import java.util.concurrent.atomic.AtomicInteger

/**
 * Extract Dtmf to Macro mappings.
 * These are spread out in variouos places in [[net.wa9nnn.rc210.serial.Memory]] to se get them all then build a map to macros can access them
 */
object DtmfMacroExractor {

  def apply(implicit memory: Memory): DtmfMacros = {
    val mai = new AtomicInteger(1) // cause macro numbers start at 1


   def dtmfMap(slicePos: SlicePos, memory: Memory): Seq[(MacroKey, Dtmf)] = {
      val macrosSlice = memory(slicePos)
      macrosSlice.data
        .grouped(5)
        .toSeq
        .map { bytes =>
          MacroKey(mai.getAndIncrement()) -> Dtmf(bytes)
        }
    }

    val longMacroDtmf = dtmfMap(SlicePos("//MacroRecallCode - 2625-2824"), memory)
    val shortMacroDtmf = dtmfMap(SlicePos("//ShortMacroRecallCode - 3175-3424"), memory)
    DtmfMacros(longMacroDtmf.concat(shortMacroDtmf))
  }
}

case class DtmfMacros(a: Seq[(MacroKey, Dtmf)]) {
  private val map: Map[MacroKey, Dtmf] = a.toMap
  val ordered: Seq[MacroKey] = map.keys.toSeq.sortBy { k =>
    (k.kind, k.number)
  }

  def apply(macroKey: MacroKey): Dtmf = {
    map.getOrElse(macroKey, Dtmf())
  }
}
