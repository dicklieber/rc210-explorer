package net.wa9nnn.rc210.serial

import org.specs2.mutable.Specification

import java.nio.file.{Files, Paths}

class MemorySpec extends Specification {
  private val data = Array[Int](1, 2, 3)
  private val comPort = ComPort("desc", "fred")
  private val expected = Memory(data, comPort)

  "MemoryTest" should {
    "round trip" in {
      val logsDir = Paths.get("logs")
      val path = Files.createTempFile(logsDir, "Memory-", ".json")
      expected.save(path)

      val backAgain = Memory(path)

      backAgain.data must beEqualTo (expected.data)
    }

    "get one int" in {
      expected(2) must beEqualTo (3)
    }
    "slice" in {
      val array: Array[Int] = expected(1, 2)
      array must haveLength(2)
      array.head must beEqualTo (2)
      array.last must beEqualTo (3)
    }

  }
}
