package net.wa9nnn.rc210.serial

import org.specs2.mutable.Specification

import java.nio.file.{Files, Paths}
import scala.util.Try

class MemorySpec extends Specification {
  private val data = Array[Int](1, 2, 3, 0 ,255)
  private val comment = "justg a comment"
  private val expected = Memory(data, comment)

  "MemoryTest" should {
    "round trip" in {
      val logsDir = Paths.get("logs")
      Files.createDirectories(logsDir)
      val path = Files.createTempFile(logsDir, "Memory-", ".txt")
      expected.save(path)

      val backAgain: Try[Memory] = Memory(path)

      val backAgainMemory = backAgain.get
      backAgainMemory.data must beEqualTo (expected.data)
      backAgainMemory.comment must beEqualTo (comment)
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
