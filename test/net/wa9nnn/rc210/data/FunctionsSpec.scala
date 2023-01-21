package net.wa9nnn.rc210.data

import org.specs2.mutable.Specification

class FunctionsSpec extends Specification {
  val functions = new Functions()

  "Functions" should {
    "get1" in {
      val function = functions.get(1).get
      function.id must beEqualTo (1)
      function.description must beEqualTo ("Port 1 CTCSS Access")
    }
    "get2" in {
      val function = functions.get(2).get
      function.id must beEqualTo (2)
      function.description must beEqualTo ("Port 2 CTCSS Access")
      function.callMacro must beNone
    }
    "call macro" in {
      val function = functions.get(901).get
      function.callMacro must beSome(1)
      function.toString must beEqualTo ("function: 901 description: Call Macro 1")
    }
  }
}
