package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.RcSpec
import org.scalatest.Assertion
import org.scalatest.TryValues.*
import org.scalatest.matchers.must.Matchers.mustEqual


class NamedKeyExtractorTest extends RcSpec:
  "NamedKeyExtractorTest" should {
    "parse out named keys" in {
      val r: Iterator[(String, Seq[String])] = BuildFormData()
      
      r.toSeq.toString() mustEqual  ("")

      /*      r match
              case Failure(exception) =>
                throw exception
              case Success(data: Map[String, Seq[String]]) =>
                val namedKeys = NamedKeyExtractor(data)
                namedKeys must have length (3)
      */
    }

  }


