package net.wa9nnn.rc210.command

import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

import scala.util.Try

class ParsersSpec extends Specification with DataTables {

  "DTMF" >> {
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
  "Boolean" >> {
    "In" | "Result" |
      Array(0) !! false |
      Array(1) !! true |
      Array(255) !! true |> { (in:Array[Int], result:Boolean) =>
      val triedValue: Try[ItemValue] = BooleanParser(in)
      triedValue must beSuccessfulTry
      val i: ItemBoolean = triedValue.get.asInstanceOf[ItemBoolean]
      i.value must beEqualTo(result)
    }
  }

}
