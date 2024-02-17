package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.RcSpec
import org.scalatest.Assertion
import org.scalatest.TryValues.*
import org.scalatest.matchers.must.Matchers.mustEqual
import net.wa9nnn.rc210.ui.BuildFormData

class NamedKeyExtractorTest extends RcSpec {
  "NamedKeyExtractor" should {
    "have namedKey" in {
      val data: Map[String, Seq[String]] = BuildFormData()
      val namedKeys = NamedKeyExtractor(data)
      namedKeys must have length (3)
    }
    "Node" in {
      val data: Map[String, Seq[String]] = Map.empty
      val namedKeys = NamedKeyExtractor(data)
      namedKeys must have length (0)
    }

  }
}

