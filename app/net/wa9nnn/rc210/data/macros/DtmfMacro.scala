package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.key.KeyFactory.MacroKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.MemoryBuffer

import java.util.concurrent.atomic.AtomicInteger

/**
 * Extract Dtmf to Macro mappings.
 * These are spread out in variouos places in [[net.wa9nnn.rc210.serial.Memory]] to se get them all then build a map to macros can access them
 */
object DtmfMacroExtractor {

  def apply(memoryBuffer: MemoryBuffer): DtmfMacros = {
    val mai = new AtomicInteger(1) // cause macro numbers start at 1


    def dtmfMap(offset: Int, nDtmfs: Int): Seq[(MacroKey, Dtmf)] = {
      val chunks: Seq[Array[Int]] = memoryBuffer.chunks(offset, 5, nDtmfs)

      chunks.map { chunk =>
        val key: MacroKey = KeyFactory.macroKey(mai.getAndIncrement())
        key -> Dtmf(chunk.toIndexedSeq)
      }
    }

    val longMacroDtmf: Seq[(MacroKey, Dtmf)] = dtmfMap(2625, 40) //SlicePos("//MacroRecallCode - 2625-2824"))
    val shortMacroDtmf: Seq[(MacroKey, Dtmf)] = dtmfMap(3175, 50) //SlicePos("//ShortMacroRecallCode - 3175-3424"))
    DtmfMacros(longMacroDtmf.concat(shortMacroDtmf))
  }
}

case class DtmfMacros(a: Seq[(MacroKey, Dtmf)]) {
  private val map: Map[MacroKey, Dtmf] = a.toMap
  val ordered: Seq[MacroKey] = map.keys.toSeq.sortBy { k =>
    (k.kind, k.number)
  }

  def apply(macroKey: MacroKey): Option[Dtmf] = {
    map.get(macroKey)
  }
}
