package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.fixtures.WithMemory
import org.specs2.mutable.Specification

class DtmfMacroExractorSpec extends WithMemory {

  "DtmfMacroExractor" should {
    "apply" in {
      val dtmfMacros: DtmfMacros = DtmfMacroExractor(memory)
      val ordered = dtmfMacros.ordered
      ordered.head.index must beEqualTo (1)
      ordered.last.index must beEqualTo (90)
    }
  }
}
