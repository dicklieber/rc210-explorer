package net.wa9nnn.rc210

import org.specs2.mutable.Specification

import java.net.URL
import java.nio.file.{Path, Paths}

class DatFileIoSpec extends Specification {

  "DatFileIoSpec" should {
    "From string" in {
      val expectedSectionName = "SectionName"
      val fileString =
        """[""" + expectedSectionName +
          """]
            |DOW(3)=0
            |MonthToRun(3)=0
            |Monthly(3)=0
            |Week(3)=1
            |Hours(3)=07
            |Minutes(3)=00
            |MacroToRun(3)=03
            |""".stripMargin

      val datFile = DatFileParser(fileString)

      val datSection = datFile.section(expectedSectionName)
      datSection.sectionName must beEqualTo(expectedSectionName)
      datSection.dataItems must haveSize(7)
      datFile.size must beEqualTo(1)
    }

    "From file" in {
      val url1: URL = getClass.getResource("/data/schedExamples.dat")
      val path: Path = Paths.get(url1.toURI)
      val datFile = DatFileParser(path)
      datFile.size must beEqualTo(25)
    }
  }
}
