package net.wa9nnn.rc210.model.macros

import net.wa9nnn.rc210.NumberedValues
import net.wa9nnn.rc210.data.FunctionNodeId
import net.wa9nnn.rc210.model.DatFile

import scala.util.Try

object Macros {

  def apply(datFile: DatFile): List[MacroNode] = {
    datFile.section("Macros")
      .process[MacroNode] { numberedValues =>
        build(numberedValues)
      }
  }

   def build(implicit numberedValues: NumberedValues): Try[MacroNode] = {
    {
      import NumberedValues._
      Try {
        val dtmf: String = vs("MacroCode")
        MacroNode(MacroNodeId(numberedValues.number.get),
          Option.when(dtmf.nonEmpty)(dtmf),
          vs("Macro")
            .split(" ")
            .toList.map(sNumber => FunctionNodeId(sNumber.toInt))
        )
      }
    }
  }
}
