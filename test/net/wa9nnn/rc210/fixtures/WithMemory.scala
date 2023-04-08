package net.wa9nnn.rc210.fixtures

import net.wa9nnn.rc210.serial.{MemoryArray, MemoryBuffer}
import org.specs2.mutable.Specification

import java.net.URL

class WithMemory extends Specification{
  private val url: URL = getClass.getResource("/data/MemExample.txt")
  private val memory: MemoryArray = MemoryArray(url).get
  implicit val memoryBuffer = new MemoryBuffer(memory.data)

}
