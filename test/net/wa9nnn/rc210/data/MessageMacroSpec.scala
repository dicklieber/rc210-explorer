package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.WithDatFile

class MessageMacroSpec extends WithDatFile {

  "MessageMacroSpec" should {
    "apply" in {
      val messageMacros: MessageMacros = MessageMacro(datFile)
      messageMacros.messaageMacros must haveSize(11)
      val id = MessageMacroId(1)
      val mmn: MessageMacro = messageMacros(id)
      mmn.nodeId must beEqualTo(id)
      mmn.value must beEqualTo("USE(VERB) P L ONE O SEVEN POINT TWO")
    }
  }
}
