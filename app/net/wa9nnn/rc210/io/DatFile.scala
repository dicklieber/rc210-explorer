/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wa9nnn.rc210.io

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.TimeConverters.fileStamp
import configs.syntax._
import net.wa9nnn.rc210.serial.{Memory, RC210Data}

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, Path}
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class DatFile @Inject()(config: Config) extends LazyLogging {

  val memoryFile: Path = config.get[Path]("vizRc210.memoryFile").value
  private val historyDir: Path = config.get[Path]("vizRc210.historyDir").value
  private val dataStoreFile: Path = config.get[Path]("vizRc210.dataStoreFile").value

  def load(): Try[Memory] = {
    Memory.load(memoryFile.toUri.toURL)
  }
  def apply(rc210Data: RC210Data): Memory = {
    val memory = rc210Data.mainArray
    val extMemory = rc210Data.extArray
    logger.info(rc210Data.progress.toString)
    val portDescription: String = rc210Data.serialPort.getPortDescription
    // build MemoryArray
    val data = memory.concat(extMemory)
    val newMemory = new Memory(data)
    // backup old
    if(Files.exists(memoryFile)){
      val fileTime: Instant = Files.getLastModifiedTime(memoryFile).toInstant
      val stampName = fileStamp(fileTime)
      val historic: String = memoryFile.getFileName.toString .replace(".", stampName + ".")
      val target = historyDir.resolve(historic)
      Files.createDirectories(historyDir)
      Files.move(memoryFile, target)
    }

    // save new
    newMemory.save(memoryFile)
    newMemory
  }

  def save(writer: PrintWriter => Unit): Unit = {
    throw new NotImplementedError()
  }

}
