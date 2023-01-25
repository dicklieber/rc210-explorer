package net.wa9nnn.rc210.model.macros

import net.wa9nnn.rc210.NumberedValues
import net.wa9nnn.rc210.data.FunctionNodeId
import net.wa9nnn.rc210.model.DatFile

import scala.util.Try

object Macros {

  def apply(datFile: DatFile): List[Macro] = {
    datFile.section("Macros")
      .process[Macro] { numberedValues =>
        build(numberedValues)
      }
  }

   def build(implicit numberedValues: NumberedValues): Try[Macro] = {
    {
      import NumberedValues._
      Try {
        val dtmf: String = vs("MacroCode")
        Macro(MacroNodeId(numberedValues.number.get),
          Option.when(dtmf.nonEmpty)(dtmf),
          vs("Macro")
            .split(" ")
            .toList.map(sNumber => FunctionNodeId(sNumber.toInt))
        )
      }
    }
  }
}
