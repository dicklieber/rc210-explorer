package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.fixtures.WithMemory

class MacroNodeExcractorSpec extends WithMemory {

  "Macro" should {
    "apply" in {
      val macros: Seq[MacroNode] = MacroExtractor(memory)
      macros must haveLength(90)
    }
  }
}
