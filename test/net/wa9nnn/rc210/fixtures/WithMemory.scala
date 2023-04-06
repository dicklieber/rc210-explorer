package net.wa9nnn.rc210.fixtures

import net.wa9nnn.rc210.serial.{Memory, MemoryArray, MemoryBuffer}
import org.specs2.mutable.Specification

import java.io.InputStream
import java.nio.file.Paths

class WithMemory extends Specification{
  private val stream: InputStream = getClass.getResourceAsStream("/data/MemExample.txt")
  implicit val memory: MemoryArray = MemoryArray(stream).get
//  val memory: Memory = Memory.apply(getClass.getResourceAsStream("/data/MemExample.txt")).get

  val memoryBuffer = new MemoryBuffer(memory.data)

}
