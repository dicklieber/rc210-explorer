package net.wa9nnn.rc210.data

import org.specs2.mutable.Specification

class DtmfSpec extends Specification {
  val ints:Seq[Int] = Seq(
    161,
    169,
    0xe1,
    0,
    255
  )


  "Dtmf" should {
    "parse from Seq[Int]" in {
      val dtmf = Dtmf(ints)
      dtmf.toString must beEqualTo ("10901B")

    }
  }
}
