package net.wa9nnn.rc210.serial

import org.specs2.mutable.Specification

class SlicePosSpec extends Specification {

  "SlicePos" should {
    "apply" in {
      val slicePos = SlicePos("//DTMFEnable - 70-72")
      slicePos.offset must beEqualTo(70)
      slicePos.length must beEqualTo(3)
    }
  }
}
