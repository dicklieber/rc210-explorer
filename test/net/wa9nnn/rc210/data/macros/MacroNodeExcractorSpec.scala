package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.fixtures.WithMemory

class MacroNodeExcractorSpec extends WithMemory {

  "Macro" should {
    "apply" in {
      val mne = new MacroExtractor()
      val rc210D = mne(memory, new Rc210Data())
      rc210D.macros must haveLength(90)
    }
  }
}
