package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.fixtures.WithMemory
import org.specs2.mutable.Specification

class DtmfMacroExractorSpecNode extends WithMemory {

  "DtmfMacroExractor" should {
    "apply" in {
      val dtmfMacros: DtmfMacros = DtmfMacroExractor(memory)
      val ordered = dtmfMacros.ordered
      ordered.head.number must beEqualTo (1)
      ordered.last.number must beEqualTo (90)
    }
  }
}
