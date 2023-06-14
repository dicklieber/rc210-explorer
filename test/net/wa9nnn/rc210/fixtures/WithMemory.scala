package net.wa9nnn.rc210.fixtures

import net.wa9nnn.RcSpec
import net.wa9nnn.rc210.serial.Memory

import java.net.URL
import scala.util.{Failure, Success}

class WithMemory extends RcSpec {
  private val url: URL = getClass.getResource("/data/MemExample.txt")
  val memory: Memory = Memory.load(url) match {
    case Failure(exception) =>
      throw exception
    case Success(memory: Memory) =>
      memory
  }
}
