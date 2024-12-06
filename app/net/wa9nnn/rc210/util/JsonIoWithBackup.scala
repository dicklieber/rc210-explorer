package net.wa9nnn.rc210.util

import os.*
import play.api.libs.json.*

/**
 * reads/write json files with backup of previous file.
 *
 */
object JsonIoWithBackup {

  def apply(baseFile: os.Path, jsValue: JsValue): Unit = {
    val parent: os.Path = baseFile / os.up
    os.makeDir.all(parent)
    val baseFilerName = baseFile.baseName
    val temp: os.Path = parent / (baseFilerName + ".temp")
    val backup: os.Path = parent / (baseFilerName + ".backup")
    os.remove(temp)

    val sJson = Json.prettyPrint(jsValue)
    os.write(temp, sJson)
    if os.exists(baseFile)  then 
      os.move(baseFile, backup, replaceExisting = true)
    
    os.move(temp, baseFile)
  }

  def apply(baseFile: os.Path): JsValue = {
    val str: String = os.read(baseFile)
    Json.parse(str)
  }
}

