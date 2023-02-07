package net.wa9nnn.rc210.serial

import org.specs2.mutable.Specification

import java.nio.file.{Files, Paths}
import scala.util.Try

class MemorySpec extends Specification {
  private val data = Array[Int](1, 2, 3, 0 ,255)
  private val comment = "justg a comment"
  private val memory = MemoryArray(data, comment)

  "MemoryTest" should {
    "round trip" in {
      val logsDir = Paths.get("logs")
      Files.createDirectories(logsDir)
      val path = Files.createTempFile(logsDir, "Memory-", ".txt")
      memory.save(path)

      val backAgain: Try[MemoryArray] = MemoryArray(path)

      val backAgainMemory = backAgain.get
      backAgainMemory.data must beEqualTo (memory.data)
      backAgainMemory.comment must beEqualTo (comment)
    }

    "get one int" in {
      val slice: Slice = memory(SlicePos(2, 1))
      val expected: Seq[Int] = Seq(3)
      slice.data must beEqualTo (expected)
    }
    "slice" in {
      val slice: Slice = memory(SlicePos(1, 2))
      slice.data must haveLength(2)
      slice.head must beEqualTo (2)
      slice.data.last must beEqualTo (3)
    }

  }
}
