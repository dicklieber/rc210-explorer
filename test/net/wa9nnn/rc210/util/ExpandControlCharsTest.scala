package net.wa9nnn.rc210.util

import net.wa9nnn.rc210.RcSpec

class ExpandControlCharsTest extends RcSpec {

  "ExpandControlCharsTest" should {
    "get" when {
      "has control char" in {
        val out = expandControlChars("\rHello\nlfBeforeThis")
        out mustBe("␍Hello␊lfBeforeThis")
        val head: Long = out.head.toLong
        head mustBe(0x240d)
      }
      "empty string" in {
        val out = expandControlChars("")
        out mustBe("")
      }
    }
  }
}
