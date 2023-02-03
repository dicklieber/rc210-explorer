package net.wa9nnn.rc210.fixtures

import net.wa9nnn.rc210.serial.Memory
import org.specs2.mutable.Specification

class WithMemory extends Specification{
  val memory: Memory = Memory.apply(getClass.getResourceAsStream("/data/MemExample.txt")).get


}
