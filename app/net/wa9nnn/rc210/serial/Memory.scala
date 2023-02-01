package net.wa9nnn.rc210.serial

import boopickle.Default._

import java.nio.ByteBuffer
import java.nio.file.{Files, Path}

case class Memory(data: Array[Int], comPort: ComPort, comment:String = "")  {
  def apply(index:Int):Int = {
    data(index)
  }

  def apply(index:Int, length:Int):Array[Int] = data.slice(index, index + length  )

  def save(path: Path): Unit = {
    Files.write(path, Pickle.intoBytes(this).array())
  }
}

object Memory {
  def apply(path: Path): Memory = {
    val buf = ByteBuffer.wrap(Files.readAllBytes(path))
    val memory: Memory = Unpickle[Memory].fromBytes(buf)
    memory
  }

}