package net.wa9nnn.model

import net.wa9nnn.rc210.RawDataLine
import org.specs2.mutable.Specification

import scala.util.Try

class NumberSectionSpec extends Specification {
  private val sameNumbers =
    """DOW(3)=0
      |MonthToRun(3)=0
      |Monthly(3)=0
      |Week(3)=1
      |Hours(3)=07
      |Minutes(3)=00
      |MacroToRun(3)=03
      |""".stripMargin
  private val notsameNumbers =
    """DOW(4)=0
      |MonthToRun(4)=0
      |Monthly(1)=0
      |Week(4)=1
      |Hours(4)=07
      |Minutes(4)=00
      |MacroToRun(4)=03
      |""".stripMargin

  def parser(lines: String): List[DataItem] = {
   val r: List[DataItem] =  lines
      .split("\n")
      .zipWithIndex
      .map { case (line, lineNumber) =>
       DataItem(RawDataLine(line, lineNumber))
      }.toList
    r
  }

  "NumberSectionSpec" should {
    "same good" in {
      val value1: List[DataItem] = parser(notsameNumbers)

      NumberSection(value1) must throwAn[IllegalArgumentException]
    }
    "different numbers" in {
      val value1: List[DataItem] = parser(sameNumbers)

      val numberSection = NumberSection(value1)
      numberSection.number must beEqualTo(3)
      numberSection.items must haveSize(7)
    }
  }
}
