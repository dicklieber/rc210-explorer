package net.wa9nnn.rc210.ui

object Testor extends App:
  val r: Iterator[(String, Seq[String])] = BuildFormData()

  val data: Map[String, Seq[String]] = r.toMap
  val namedKeys = NamedKeyExtractor(data)
  println(namedKeys)
