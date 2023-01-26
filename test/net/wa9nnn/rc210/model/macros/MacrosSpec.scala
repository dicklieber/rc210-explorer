package net.wa9nnn.rc210.model.macros

import net.wa9nnn.rc210.DatFileParser
import net.wa9nnn.rc210.model.DatFile
import org.specs2.mutable.Specification

class MacrosSpec extends Specification {
  private val datFile: DatFile = DatFileParser(getClass.getResourceAsStream("/examples/schedExamples.dat"))
  "MacrosSpec" should {
    "build" in {
      val macros = Macros(datFile)
      ok
    }
  }
}