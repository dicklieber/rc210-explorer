package net.wa9nnn.rc210.fixtures

import net.wa9nnn.rc210.serial.Memory
import org.specs2.mutable.Specification

import java.nio.file.Paths

class WithMemory extends Specification{
  val memory: Memory = Memory.apply(Paths.get("/Users/dlieber/dev/ham/rc210-explorer/logs/Mem16918954127124727059.txt")).get
//  val memory: Memory = Memory.apply(getClass.getResourceAsStream("/data/MemExample.txt")).get


}
