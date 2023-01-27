package net.wa9nnn.rc210

import net.wa9nnn.rc210.model.DatFile
import org.specs2.mutable.Specification

trait WithDatFile extends Specification {
   val datFile: DatFile = DatFileParser(getClass.getResourceAsStream("/examples/schedExamples.dat"))

}
