package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.ItemValue.Values
import net.wa9nnn.rc210.command.Parsers.ParsedValues
import net.wa9nnn.rc210.serial.Slice
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

import scala.language.implicitConversions
import scala.util.Try

class ParsersSpec extends Specification with DataTables {
  implicit def s2slice(s: String): Slice = Slice(s)

  "DTMF" >> {
    "In" | "Result" |
      "56, 53, 0, 0, 255, 255" !! "85" |
      "49, 68, 67, 50" !! "1DC2" |
      "50, 0, 0, 0" !! "2" |
      "0, 0, 0, 0" !! "" |> { (in: String, result: String) =>
      val triedValue: ParsedValues = DtmfParser(Command.TTPadTest, in)
      val i: String = triedValue.head.head
      i must beEqualTo(result)
    }
  }
  //  "Boolean" >> {
  //    "In" | "Result" |
  //      Array(0) !! false |
  //      Array(1) !! true |
  //      Array(255) !! true |> { (in:Array[Int], result:Boolean) =>
  //      val triedValue: Try[ItemValue] = BooleanParser(in)
  //      triedValue must beSuccessfulTry
  //      val i: ItemBoolean = triedValue.get.asInstanceOf[ItemBoolean]
  //      i.value must beEqualTo(result)
  //    }
  //  }

}
