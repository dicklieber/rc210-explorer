
package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.model.{DatFile, DataItem}
import org.apache.commons.io.IOUtils

import java.io.InputStream
import java.nio.file.{Files, Path}
import scala.io.{BufferedSource, Source}

/**
 * Know how to parse an RCP .dat file
 */
object DatFileParser {
  private val header = """\[(.+)]""".r

  def apply(file: Path): DatFile = {
    apply(new BufferedSource(Files.newInputStream(file)))
  }

  def apply(inputStream: InputStream): DatFile = {
    apply(new BufferedSource(inputStream))
  }
  def apply(string: String): DatFile = {
    apply(new BufferedSource(IOUtils.toInputStream(string, "UtF-8")))
  }

  def apply(source: Source): DatFile = {
    val sectionsBuilder = Seq.newBuilder[DatSection]
    var currentSectionBuilder: Option[SectionBuilder] = None

    source.getLines()
      .zipWithIndex
      .foreach { case (line, index) =>
        line match {
          case header(sectName) =>
            currentSectionBuilder.foreach { sb =>
              sectionsBuilder += sb.result
            }
            currentSectionBuilder = Option(new SectionBuilder(sectName))

          case "" =>
          // ignore empty line

          case dataLine: String =>
            currentSectionBuilder.get.appendLine(dataLine, index)
        }
      }
    currentSectionBuilder.foreach { sb =>
      sectionsBuilder += sb.result
    }

    val datSections: Seq[DatSection] = sectionsBuilder.result()
    new DatFile(datSections)
  }
}


case class RawDataLine(line: String, index: Int) {
  override def toString: String = s"$index: $line"
}

class SectionBuilder(name: String) extends LazyLogging {
  private val entriesBuilder = List.newBuilder[DataItem]

  def appendLine(dataLine: String, index: Int): Unit = {
    val debugInfo = RawDataLine(dataLine, index)
    entriesBuilder += DataItem(debugInfo)
  }

  def result: DatSection = DatSection(name, entriesBuilder.result())
}