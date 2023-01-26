package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.bubble.NodeId
import org.specs2.mutable.Specification

class FunctionsSpec extends Specification {
  val functions = new Functions()

  "Functions" should {
    "get1" in {
      val function: Function = functions.get(NodeId("f3")).get
      function.nodeId must beEqualTo (1)
      function.description must beEqualTo ("Port 1 CTCSS Access")
    }
//    "get2" in {
//      val function = functions.get(2).get
//      function.nodeId must beEqualTo (2)
//      function.description must beEqualTo ("Port 2 CTCSS Access")
//      function.outGoing must beNone
//    }
//    "call macro" in {
//      val function = functions.get(901).get
//      function.outGoing must beSome(1)
//      function.toString must beEqualTo ("function: 901 description: Call Macro 1")
//    }
  }
}