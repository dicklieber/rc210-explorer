package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.data.Dtmf.Dtmf
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.util.Chunk
import net.wa9nnn.rc210.{Key, KeyKind}

import java.util.concurrent.atomic.AtomicInteger

/**
 * Extract Dtmf to Macro mappings.
 * These are spread out in variouos places in [[net.wa9nnn.rc210.serial.Memory]] 
 * to get them all then build a map to macros can access them
 */
object DtmfMacroExtractor {

  def apply(memory: Memory): DtmfMacros = {
    val mai = new AtomicInteger(1) // cause macro numbers start at 1


    def dtmfMap(offset: Int, nDtmfs: Int): Seq[(Key, Dtmf)] = {
      val chunks: Seq[Chunk] = memory.chunks(offset, 5, nDtmfs)

      for {
        chunk <- chunks
        macroKey: Key = Key(KeyKind.Macro, mai.getAndIncrement())
        dtmf <- Dtmf.fromMemoryInts(chunk.ints)
      } yield {
        macroKey -> dtmf
      }
    }

    val longMacroDtmf: Seq[(Key, Dtmf)] = dtmfMap(2625, 40) //SlicePos("//MacroRecallCode - 2625-2824"))
    val shortMacroDtmf: Seq[(Key, Dtmf)] = dtmfMap(3175, 50) //SlicePos("//ShortMacroRecallCode - 3175-3424"))
    DtmfMacros(longMacroDtmf.concat(shortMacroDtmf))
  }
}

/**
 * DTMF strings associated with Macro Keys.
 * @param a
 */
case class DtmfMacros(a: Seq[(Key, Dtmf)]) {
  private val map: Map[Key, Dtmf] = a.toMap

  def apply(macroKey: Key): Option[Dtmf] = {
    map.get(macroKey)
  }
}
