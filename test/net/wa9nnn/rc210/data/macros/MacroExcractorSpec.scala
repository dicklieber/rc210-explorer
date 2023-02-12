package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.fixtures.WithMemory

class MacroExcractorSpec extends WithMemory {

  "Macro" should {
    "apply" in {
      val macros: Seq[Macro] = MacroExtractor(memory)
      macros must haveLength(90)
    }
  }
}
