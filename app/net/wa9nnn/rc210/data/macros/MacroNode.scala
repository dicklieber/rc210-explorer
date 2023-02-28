package net.wa9nnn.rc210.data.macros

import com.google.inject.multibindings.{ProvidesIntoMap, ProvidesIntoSet}
import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.{Dtmf, Rc210Data}
import net.wa9nnn.rc210.key.{FunctionKey, MacroKey}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import net.wa9nnn.rc210.{Extractor, MemoryExtractor}

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

case class MacroNode(key: MacroKey, dtmf: Dtmf, functions: Seq[FunctionKey]) extends TriggerNode {
  override def nodeEnabled: Boolean = functions.nonEmpty


  override def toRow: Row = {
    val sFunctionString = functions
      .map { fk =>
        fk.number
      }.mkString(" ")
    Row(key.toCell, dtmf, sFunctionString)
      .withId(key.toString)
  }


  def table()(implicit rc210Data: Rc210Data, functionsProvider: FunctionsProvider): Table = {
    Table(Seq.empty, for {
      functionKey <- functions
      f <- functionsProvider(functionKey)
    } yield {
      f.toRow
    })
  }

  override def macroToRun: MacroKey = key

  override def triggerEnabled: Boolean = dtmf.enabled

  override def triggerDescription: String = s"Run on DTMF: $dtmf"
}

object MacroNode {
  def header(count: Int): Header = Header(s"Macros ($count)", "Key", "DTMF", "Functions")

}

//@ProvidesIntoSet
//@Singleton
class MacroExtractor extends MemoryExtractor {
  def apply(memory: Memory, rc210Data: Rc210Data): Rc210Data = {
    val dtmfMacroMap: DtmfMacros = DtmfMacroExractor(memory)
    val mai = new AtomicInteger(1)


    def macroBuilder(macroSlicePos: SlicePos, memory: Memory, bytesPerMacro: Int) = {
      memory(macroSlicePos).data
        .grouped(bytesPerMacro)
        .map { bytes =>
          val functions: Seq[FunctionKey] = bytes.takeWhile(_ != 0).map(fn => FunctionKey(fn))
          val macroKey = MacroKey(mai.getAndIncrement())
          MacroNode(macroKey, dtmfMacroMap(macroKey), functions)
        }.toSeq
    }

    val longMacros: Seq[MacroNode] = macroBuilder(SlicePos("//Macro - 1985-2624"), memory, 16)
    val shortMacros: Seq[MacroNode] = macroBuilder(SlicePos("//ShortMacro - 2825-3174"), memory, 7)

    rc210Data.copy(macros = longMacros.concat(shortMacros))

  }
}


