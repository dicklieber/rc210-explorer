package net.wa9nnn.rc210.data

import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import net.wa9nnn.rc210.DatFileParser
import net.wa9nnn.rc210.bubble.TriggerNode
import net.wa9nnn.rc210.model.DatFile
import net.wa9nnn.rc210.model.macros.{MacroNodeId, Macros}
import org.specs2.mutable.Specification

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class BuilderHelpersSpec extends Specification {
  private val datFile: DatFile = DatFileParser(getClass.getResourceAsStream("/examples/schedExamples.dat"))
  "macroTriggers" >> {
    val map: mutable.Map[MacroNodeId, ListBuffer[TriggerNode]] = BuilderHelpers.macroToTriggers(datFile)
    val triggers = map(MacroNodeId(3))
    triggers must haveSize(1)
    triggers.head.nodeId.number  must beEqualTo (3)


  }
}
