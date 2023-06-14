package net.wa9nnn.rc210.data.functions

import net.wa9nnn.RcSpec
import net.wa9nnn.rc210.key.KeyFactory.{FunctionKey, MacroKey}

class FunctionsSpec extends RcSpec {

  "Functions" should {
    "load" in {
      val functions = new FunctionsProvider()
      functions(FunctionKey(1)).get.toString  should equal ("FunctionNode(functionKey1,Port 1 CTCSS Access,None)")
      functions.size should equal (872)

      val invokedMacros = functions.invokedMacros
      invokedMacros.head should equal (MacroKey(1))

    }
  }
}
