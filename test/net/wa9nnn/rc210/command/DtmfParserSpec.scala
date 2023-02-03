package net.wa9nnn.rc210.command

import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

class DtmfParserSpec extends Specification with DataTables {

  "DtmfParser" >> {
    "In" | "Result" |
      Array(49, 68, 67, 50) !! "1DC2" |
      Array(50,0,0,0) !! "2" |
      Array(0,0,0,0) !! "" |> { (in:Array[Int], result:String) =>
      val triedValue = DtmfParser(in)
      triedValue must beSuccessfulTry
      val i: ItemString = triedValue.get.asInstanceOf[ItemString]
      i.value must beEqualTo(result)
    }
  }

}
