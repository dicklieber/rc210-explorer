package net.wa9nnn.rc210.util

import com.wa9nnn.util.TimeConverters._
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.apache.commons.io.file.PathUtils
import play.api.libs.json.{JsValue, Json}

import java.nio.file._
import java.nio.file.attribute.FileTime
import java.util.UUID
import scala.util.Try

/**
 * reads/write json files with backup of previous file.
 *
 */
object JsonIoWithBackup {

  def apply(baseFile: Path, jsValue: JsValue): Unit = {
    val parent = baseFile.getParent
    Files.createDirectories(parent)
    val backup: Path = baseFile.resolveSibling(baseFile.getFileName.toFile.getName + ".backup")
    val temp: Path = baseFile.resolveSibling(baseFile.getFileName.toFile.getName + ".temp")
    Files.deleteIfExists(temp)

    val sJson = Json.prettyPrint(jsValue)
    Files.writeString(temp, sJson)
    Files.deleteIfExists(backup)
    if (Files.exists(baseFile))
      Files.move(baseFile, backup)
    Files.move(temp, baseFile)
  }


  def apply(baseFile: Path): JsValue = {
      Json.parse(Files.readString(baseFile))
  }
}

