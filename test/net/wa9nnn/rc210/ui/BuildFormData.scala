package net.wa9nnn.rc210.ui

object BuildFormData:
  def apply(): Iterator[(String, Seq[String])] =
    val bufferedSource = BufferedSource(getClass.getResourceAsStream("/namedKeysDataPorts.txt"))
    val r: Iterator[(String, Seq[String])] = bufferedSource
      .getLines().map { (line: String) =>
        try
          println(line)
          val parts: Array[String] = line.split(":")
          println(parts)
          val tuple = s"$parts(0):$parts(1)" -> Seq(parts(2))
          println(tuple)
          tuple
        catch
          case e: Exception =>
            e.printStackTrace()
            "throw e" -> Seq(e.getMessage)
      }
