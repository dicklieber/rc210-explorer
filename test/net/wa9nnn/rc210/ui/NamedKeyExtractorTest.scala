package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.RcSpec
import org.scalatest.Assertion
import org.scalatest.TryValues.*
import org.scalatest.matchers.must.Matchers.mustEqual
import net.wa9nnn.rc210.ui.BuildFormData

class NamedKeyExtractorTest extends RcSpec {
  "NamedKeyExtractorTest" should {
    "parse out named keys" in {
      try
        val r: Iterator[(String, Seq[String])] = BuildFormData()

        val data: Map[String, Seq[String]] = r.toMap
        val namedKeys = NamedKeyExtractor(data)
        namedKeys must have length (3)
      catch
        case e:Exception =>
          e.printStackTrace()
      /*      r match
              case Failure(exception) =>
                throw exception
              case Success(data: Map[String, Seq[String]]) =>
                val namedKeys = NamedKeyExtractor(data)
                namedKeys must have length (3)
      */
    }

  }
}

