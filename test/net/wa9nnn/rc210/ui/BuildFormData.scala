package net.wa9nnn.rc210.ui

import org.scalatest.matchers.must.Matchers.have

import scala.collection.immutable.Map
import scala.io.BufferedSource

object BuildFormData:
  def apply(): Iterator[(String, Seq[String])] =
    val bufferedSource: BufferedSource = BufferedSource(getClass.getResourceAsStream("/namedKeysDataPorts.txt"))
    val r = bufferedSource
      .getLines().map { (line: String) =>
        try
          println(s"lineXYZZY: $line")
          val parts: Array[String] = line.split(":")
          println("Parts:  1st")
          val tuple = s"$parts(0):$parts(1)" -> Seq(parts(2))
          println(s"tuple: $tuple")
          tuple
        catch
          case e: Throwable =>
            e.printStackTrace()
            "throw e" -> Seq(e.getMessage)
      }
    r


