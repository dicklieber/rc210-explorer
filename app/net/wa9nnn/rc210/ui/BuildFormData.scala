package net.wa9nnn.rc210.ui


import scala.collection.immutable.Map
import scala.io.BufferedSource

object BuildFormData:
  def apply(): Map[String, Seq[String]] =
    val bufferedSource: BufferedSource = BufferedSource(getClass.getResourceAsStream("/namedKeysDataPorts.txt"))
    val r: Iterator[FormData] = bufferedSource
      .getLines().map { (line: String) =>
        try
          val parts: Array[String] = line.split(":")
          FormData(parts)
        catch
          case e: Throwable =>
            throw e
      }
    r.map(fd => fd.name -> fd.values).toMap
  
case class FormData(name: String, values: Seq[String])

object FormData:
  def apply(parts: Array[String]):FormData =
    val name: String = s"${parts(0)}:${parts(1)}"
    FormData(name, Seq(parts(2)))
