package net.wa9nnn.rc210.ui


import scala.collection.immutable.Map
import scala.io.BufferedSource

object BuildFormData:
//  data: Map[String, Seq[String]] = r.map(fd => fd.name -> fd.values).toMap
  def apply(): Map[String, Seq[String]] =
    val bfd = "bfd"
    println(bfd)
    val bufferedSource: BufferedSource = BufferedSource(getClass.getResourceAsStream("/namedKeysDataPorts.txt"))
    val r: Iterator[FormData] = bufferedSource
      .getLines().map { (line: String) =>
        try
          println(s"lineXYZZY: $line")
          val parts: Array[String] = line.split(":")
          println(parts.head)
          val formData = FormData(parts )
          println(s"formData: $formData")
          formData
        catch
          case e: Throwable =>
            e.printStackTrace()
            throw e
      }
    r.map(fd => fd.name -> fd.values).toMap


case class FormData(name:String, values:Seq[String])

object FormData:
  def apply(parts: Array[String]):FormData =
    val head = parts.head
    val str0 = parts(0)
    val name: String = s"${parts(0)}:${parts(1)}"
    FormData(name, Seq(parts(2)))
