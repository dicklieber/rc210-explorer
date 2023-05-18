package net.wa9nnn.rc210.data

import org.specs2.mutable.Specification



class DtmfSpec extends Specification with org.specs2.specification.Tables {
  override def is =
    s2"""

 adding integers should just work in scala ${
      // the header of the table, with `|` separated strings (`>` executes the table)
      "ints" | "expected" |>
        Seq(0, 0, 0, 0, 255) ! "" |
        Seq(0xa, 0, 0, 0, 255) ! "0" |
        Seq(0xb, 0, 0, 0, 255) ! "*" |
        Seq(0xc, 0, 0, 0, 255) ! "#" |
        Seq(33,3, 0, 0, 0) ! "123" |
        Seq(0x21, 3, 9, 0, 255) ! "123" |
        Seq(1, 0, 9, 0, 255) ! "1" |
        Seq(0x21, 0, 9, 0, 255) ! "12" |
        Seq(161, 169, 0xe1, 0, 255) ! "10901B" | // another example row
        { (ints, expected) =>
          val dtmf = Dtmf(ints)
          dtmf.get must beEqualTo(expected)
        } // the expectation to check on each row
    }
"""
}
