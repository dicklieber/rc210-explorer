package net.wa9nnn.rc210.data.functions

import net.wa9nnn.rc210.key.{FunctionKey, MacroKey, MessageMacroKey}
import org.specs2.mutable.Specification

class FunctionsSpec extends Specification {

  "Functions" should {
    "load" in {
      val functions = new FunctionsProvider()
      functions(FunctionKey(1)).get.toString must beEqualTo ("FunctionNode(function1,Port 1 CTCSS Access,None)")
      functions.size must beEqualTo (872)

      val invokedMessageMacros = functions.invokedMessageMacros
      invokedMessageMacros.head must beEqualTo (MessageMacroKey(1))

      val invokedMacros = functions.invokedMacros
      invokedMacros.head must beEqualTo (MacroKey(1))

    }
  }
}
